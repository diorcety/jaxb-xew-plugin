# JAXB @XmlElementWrapper Plugin

## Description

This JAXB plugin utilises the power of `@XmlElementWrapper` annotation. Originally `xjc` trends to create wrapper classes which are the containers for collections. This plugin goes through all properties to find ones which can be represented in the model in more optimal way.

## The problem origin in details

To illustrate the problem let's take the following XSD:

    <?xml version="1.0" encoding="UTF-8"?>
    <xs:schema
    	xmlns:xs="http://www.w3.org/2001/XMLSchema"
    	xmlns:xml="http://www.w3.org/XML/1998/namespace"
    	elementFormDefault="qualified">
    
    	<xs:element name="order">
    		<xs:complexType>
    			<xs:sequence>
    				<xs:element ref="items" />
    			</xs:sequence>
    		</xs:complexType>
    	</xs:element>
    
    	<xs:element name="items">
    		<xs:complexType>
    			<xs:sequence>
    				<xs:element name="item" type="xs:string" maxOccurs="unbounded" />
    			</xs:sequence>
    		</xs:complexType>
    	</xs:element>
    </xs:schema>

From this XSD by default `xjc` will generate two classes:

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "items" })
    @XmlRootElement(name = "order")
    public class Order {
    
    	@XmlElement(required = true)
    	protected Items	items;
    
    	public Items getItems() {
    		return items;
    	}
    
    	public void setItems(Items value) {
    		this.items = value;
    	}
    }

and

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "item" })
    @XmlRootElement(name = "items")
    public class Items {
    
    	@XmlElement(required = true)
    	protected List<String>	item;
    
    	public List<String> getItem() {
    		if (item == null) {
    			item = new ArrayList<String>();
    		}
    		return this.item;
    	}
    }

So to access a particular item one need to write a but clumsy code `order.getItems().getItems().get(itemIndex)`. The solution is to use `@XmlElementWrapper` which cures exactly this case. The result will be only one class with direct access to items:

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "items" })
    @XmlRootElement(name = "order")
    public class Order {
    
    	@XmlElementWrapper(name = "items", required = true)
    	@XmlElement(name = "item")
    	protected List<String>	items;
    
    	public List<String> getItems() {
    		if (items == null) {
    			items = new ArrayList<String>();
    		}
    		return items;
    	}
    
    	public void setItems(List<String> items) {
    		this.items = items;
    	}
    }

## Usage

The plugin is used together with the `xjc` from the command line or from an Ant task or via `maven-jaxb2-plugin`.

The following options are applicable for plugin:

<table>
<tr>
	<th>Option</th>
	<th>Comment</th>
</tr>
<tr>
	<td>-Xxew</td>
	<td>Activate the XML Element Wrapper plugin</td>
</tr>
<tr>
	<td>-Xxew:include filename</td>
	<td>Specify a filename with candidate classes to include in the compilation.</td>
</tr>
<tr>
	<td>-Xxew:exclude filename</td>
	<td>Specify a filename with candidate classes to exclude from the compilation.</td>
</tr>
<tr>
	<td>-Xxew:summary filename</td>
	<td>Specify a filename to contain summary information for the compilation.</td>
</tr>
<tr>
	<td>-Xxew:collection FQCN</td>
	<td>Specify the class name of the collection type to use.</td>
</tr>
<tr>
	<td>-Xxew:instantiate [early|lazy|none]</td>
	<td>Specify when the collection class should be instantiated: when class is created / when property is accessed from getter / not instantiated at all.</td>
</tr>
<tr>
	<td>-Xxew:delete</td>
	<td>Delete candidate classes having been replaced during compilation.</td>
</tr>
<tr>
	<td>-Xxew:pluralForm</td>
	<td>[experimental] Apply plural form to collection property (e.g. turn "item" into "items").</td>
</tr>
</table>

### Episode file

For correct generation of episode file the corresponding XJC options should follow `-Xxew`, for example:

`-Xxew ... -episode <file>`

This will trigger episode plugin _after_ Xew plugin and episode file will be correctly generated.

### `fluent-api` and `value-constructor` plugins

These plugins should be activated _after_ Xew plugin:

`-Xxew ... -Xfluent-api -Xvalue-constructor` 

Otherwise (if they are activated before) Xew plugin cannot revert/complement the changes they made and compile-time error is guaranteed.

### `setters` plugin

These plugin should be activated _before_ Xew plugin due to problem described in [issue#15](https://github.com/dmak/jaxb-xew-plugin/issues/15):

`-Xsetters -Xxew ...` 

### Ant task

First you need to download the plugin jar (for example, from [Maven repository](http://mirrors.ibiblio.org/pub/mirrors/maven2/com/github/jaxb-xew-plugin/jaxb-xew-plugin)) and put it to your project `lib` folder.

To use the plugin from Ant you will need something like the following in your build file:

    <taskdef name="xjc" classname="com.sun.tools.xjc.XJCTask">
    	<classpath>
    		<fileset dir="${lib}/jaxb" includes="*.jar" />
    		<fileset dir="lib" includes="jaxb-xew-plugin.jar" />
    	</classpath>
    </taskdef>
    
    <xjc destdir="${src-generated}" package="dk.conspicio.example.xml2code.v2">
    	<arg value="-Xxew" />
    	<arg value="-Xxew:summary ${build}/xew-summary.txt" />
    	<arg value="-Xxew:instantiate lazy" />
    	<schema dir="xsd" includes="*.xsd" />
    	<binding dir="xsd" includes="*.xjb" />
    </xjc>

### Maven

#### maven-jaxb2-plugin

Note: `maven-jaxb2-plugin` prior to v0.8.0 was compiled against JAXB XJC API which is not compatible with this plugin. Version 0.8.1 is guaranteed to work, versions 0.8.2 and 0.8.3 should also work fine.

    <plugin>
    	<groupId>org.jvnet.jaxb2.maven2</groupId>
    	<artifactId>maven-jaxb2-plugin</artifactId>
    	<version>0.8.1</version>
    	<executions>
    		<execution>
    			<phase>generate-sources</phase>
    			<goals>
    				<goal>generate</goal>
    			</goals>
    			<configuration>
    				<verbose>true</verbose>
    				<generateDirectory>${project.build.sourceDirectory}</generateDirectory>
    				<schemaDirectory>xsd</schemaDirectory>
    				<removeOldOutput>false</removeOldOutput>
    				<episode>false</episode>
    
    				<extension>true</extension>
    				<args>
    					<arg>-no-header</arg>
    					<arg>-Xxew</arg>
    					<arg>-Xxew:instantiate lazy</arg>
    					<arg>-Xxew:delete</arg>
    				</args>
    				<plugins>
    					<plugin>
    						<groupId>com.github.jaxb-xew-plugin</groupId>
    						<artifactId>jaxb-xew-plugin</artifactId>
    						<version>1.0</version>
    					</plugin>
    				</plugins>
    			</configuration>
    		</execution>
    	</executions>
    </plugin>

#### jaxb2-maven-plugin

Note: `jaxb2-maven-plugin` v1.5 was compiled against JAXB XJC API v2.1.13 which is not compatible with this plugin, thus additional dependency is needed to be added to **plugin classpath**.

    <plugin>
    	<groupId>org.codehaus.mojo</groupId>
    	<artifactId>jaxb2-maven-plugin</artifactId>
    	<version>1.5</version>
    	<executions>
    		<execution>
    			<phase>generate-sources</phase>
    			<goals>
    				<goal>xjc</goal>
    			</goals>
    			<configuration>
    				<verbose>true</verbose>
    				<outputDirectory>${project.build.sourceDirectory}</outputDirectory>
    				<schemaDirectory>xsd</schemaDirectory>
    				<clearOutputDir>false</clearOutputDir>
    
    				<extension>true</extension>
    				<arguments>-no-header -Xxew -Xxew:instantiate lazy -Xxew:delete</arguments>
    			</configuration>
    		</execution>
    	</executions>
    	<dependencies>
    		<dependency>
    			<groupId>com.github.jaxb-xew-plugin</groupId>
    			<artifactId>jaxb-xew-plugin</artifactId>
    			<version>1.0</version>
    		</dependency>
    		<!-- 
    		 | We need to update the jaxb-xjc plugin version from 2.1.13 to the 2.2.4-1 version 
    		 | used by the jaxb-xew-plugin (version 2.1.13 which does not have the required 
    		 | method com.suun.codemodel.JAnnotatable.annotations()Ljava/util/Collection).
    		 -->
    		<dependency>
    			<groupId>com.sun.xml.bind</groupId>
    			<artifactId>jaxb-xjc</artifactId>
    			<version>2.2.4-1</version>
    		</dependency>
    	</dependencies>
    </plugin>

You can find more examples of this plugin in [`samples`](samples/) directory (including how to call this plugin from `jaxws:wsimport`).

## What's new

### v1.1

* Plugin is re-worked. Bugs fixed ([#1](https://github.com/dmak/jaxb-xew-plugin/issues/1), [#3](https://github.com/dmak/jaxb-xew-plugin/issues/3), [#6](https://github.com/dmak/jaxb-xew-plugin/issues/6), [#7](https://github.com/dmak/jaxb-xew-plugin/issues/7)). Some functionality is possible only by accessing private fields, so Xew plugin may not work in security-managed environment.
* Testing framework introduced. XSDs in [`com/sun/tools/xjc/addon/xew/`](src/test/resources/com/sun/tools/xjc/addon/xew/) directory can be referred as collection of examples.
* Logging is done via `commons-logging`. Log level is configurable like this `mvn -Dorg.apache.commons.logging.simplelog.defaultlog=DEBUG`.

### v1.0

The original code of Bjarne Hansen, with some fixes. 

## Contribution

If you have time and desire to contribute to this project you can do it in many ways:

* Improve this very documentation.
* Implement Unit tests.
* Provide more samples.

### Development

Everybody is very welcomed to send patches by email. But the best way would be:

- Fork the repository.
- Apply the [formatting rules](#code-style) (the ones for Eclipse can be found in [`dist`](dist) folder).
- Create a ticket in [bugtracker](https://github.com/dmak/jaxb-xew-plugin/issues). If applicable attach XSD that demonstrates the problem to the issue.
- Create a branch referring the ticket number (`git branch issue-22`).
- Do the changes.
- Verify your outgoing changeset. Make sure that:
  - your changeset is _minimal and sufficient_ for the feature implementation
  - your formatting rules have not caused changes in each and every line (e.g. due to end-of-line markers)
- Commit to your own fork, mentioning the ticket number in commit message (`Implemented nice feature (fixes #22)`). Check [here](https://github.com/blog/831-issues-2-0-the-next-generation) the commit message syntax sugar.
- [Request for pull](http://help.github.com/send-pull-requests/).

If you provide the code in any way you automatically agree with a [project license](#license).

#### Code style

* There are no specific coding and naming conventions for this project except ones given in [Code Conventions for the Java Programming Language](http://www.oracle.com/technetwork/java/codeconv-138413.html) by Sun. Use best practices and common sense.
* For [code formatting](dist/eclipse-code-formatting-rules.xml) basically Eclipse build-in formatting rules were used with following changes:
  - Indentation → Align fields on columns: on
  - Indentation → Tab policy: Mixed
  - Indentation → Use spaces to indent wrapped lines: on
  - Line Wrapping → Maximum line width: 120
  - Line Wrapping → Default indentation for wrapped lines: 3
  - Comments → Maximum line width for comments: 120
  - Comments → Enable line comment formatting: off
  - New Lines → Insert new line in empty anonymous class body: off
  - New Lines → Insert new line in empty block: off
* TAB is used for alignment for XML/XSD/... files. 

#### Build and release procedure

* Read [Sonatype OSS Maven Repository Usage Guide](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide) from cover to cover.
* Use the following `settings.xml` for your Maven (see [Sharing Files with Build Agents](http://wiki.cloudbees.com/bin/view/DEV/Sharing+Files+with+Build+Executors) about how to share `settings.xml` with build nodes on CloudBees):

        <settings>
        	<!-- Optional proxy configuration (if applicable to your environment) -->
        	<proxies>
        		<proxy>
        			<active>true</active>
        			<protocol>http</protocol>
        			<host>proxy</host>
        			<port>8080</port>
        			<nonProxyHosts>*.internal.domain</nonProxyHosts>
        		</proxy>
        		<proxy>
        			<active>true</active>
        			<protocol>https</protocol>
        			<host>proxy</host>
        			<port>8080</port>
        			<nonProxyHosts>*.internal.domain</nonProxyHosts>
        		</proxy>
        	</proxies>
        
        	<servers>
        		<server>
        			<id>sonatype-nexus-snapshots</id>
        			<username>...sonatype_user...</username>
        			<password>...sonatype_password...</password>
        		</server>
        		<server>
        			<id>sonatype-nexus-staging</id>
        			<username>...sonatype_user...</username>
        			<password>...sonatype_password...</password>
        		</server>
        	</servers>
        
        	<profiles>
        		<profile>
        			<id>gpg</id>
        			<properties>
        				<gpg.passphrase>...passphrase...</gpg.passphrase>
        			</properties>
        		</profile>
        	</profiles>
        </settings>

* Make sure you have git ≥ v1.7.10 installed, otherwise you may face [this bug#341221](https://bugs.eclipse.org/bugs/show_bug.cgi?id=341221).
* You need to put JAXB API ≥ v2.2.3 to `jre/lib/endorsed` directory of JDK which is used to build the project. Otherwise build will fail with `java.lang.NoSuchMethodError: javax.xml.bind.annotation.XmlElementWrapper.required()Z`.
* For Hudson freestyle job specify:
  * Pre-release step `git checkout master; git reset --hard origin/master` (see [Can't get automated release working with Hudson + Git + Maven Release Plugin](http://stackoverflow.com/questions/1877027) for more details about the problem).
  * Next step (release):  
    `release:prepare release:perform -Pstage-release,gpg -Dresume=false -Dusername=<github_user> -Dpassword=<github_password>`  
    In chain with [Mask Passwords Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Mask+Passwords+Plugin) and [Build Secret Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Build+Secret+Plugin) that can look like  
    `release:prepare release:perform -B -Dresume=false -Pstage-release -Dusername=smith -Dpassword=${SCM_PASSWORD} -Darguments="-Dgpg.homedir=${KEYS_DIR} -Dgpg.passphrase=${PGP_PASSWORD}"`

### Algorithm description

The plugin flow consists of the following parts:

* Parse arguments.
* Find classes which are candidates for removal:
  1. The candidate class should not extend any other class (as the total number of properties will be more than 1)
  2. The candidate class should have exactly one non-static property.
  3. This property should be a collection.
  4. This collection should have exactly one parametrisation type.
* Visit all classes again to check if the candidate is not eligible for removal:
  1. If there are classes that extend the candidate
  2. If there are class fields that refer the candidate by e.g. `@XmlElementRef` annotation
* Visit all classes again to replace the property having the candidate class type with collection plus `@XmlElementWrapper` annotation. On this step getters/setters are update and ObjectFactory methods are corrected. Also lazy initialization policy is applied.
* Candidates which are still marked for removal are finally removed (and ObjectFactory is updated accordingly).

There are many pitfalls in JAXB Code Model API which are forcing the developer to use dirty tricks (like accessing private fields) in order to implement the manipulation of code model. Among others:

* [JAXB-784](http://java.net/jira/browse/JAXB-784) is about NPE in `JAnnotationUse#getAnnotationMembers()` method.
* [JAXB-884](https://java.net/jira/browse/JAXB-884) is about ClassCastException in `JAnnotationArrayMember#annotations()` method. 
* [JAXB-878](https://java.net/jira/browse/JAXB-878) and [JAXB-879](https://java.net/jira/browse/JAXB-879) describe the lack of public getters for class fields.
* [JAXB-957](https://java.net/jira/browse/JAXB-957) mentions what need to be added to make it possible for the inner class to be moved to another class or package. 
* [JAXB-883](http://java.net/jira/browse/JAXB-883) does not allow to learn if "simpleMode" setting is enabled, which in its turn controls plural form for collection property names. There are however some more difficulties to overcome.

## Authors

Original code by [Bjarne Hansen](http://www.conspicio.dk/blog/bjarne/jaxb-xmlelementwrapper-plugin). Many thanks to committers:

* [Dmitry Katsubo](http://www.linkedin.com/in/dkatsubo)
* [Tobias Warneke](https://github.com/wumpz/)
* [David Matheson](https://github.com/davidfmatheson/)
* [Sebastian Steiner](https://github.com/sebisteiner/)
* [Colin Fairless](https://github.com/colin-yell/)
* [Patrick Crocker](https://github.com/patrickcrocker/)
* and others...

## License

The whole project is licensed under [LGPLv3](http://www.gnu.org/licenses/lgpl-3.0.html) (or any later version).
