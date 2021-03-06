
package annotation_reference;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for search-eu complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="search-eu">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="classes-eu" type="{}classes-eu"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "search-eu", propOrder = {
    "classesEu"
})
public class SearchEu {

    @XmlElementWrapper(name = "classes-eu", required = true, nillable = true)
    @XmlElement(name = "class-eu")
    protected List<ClassCommon> classesEu = new ArrayList<ClassCommon>();

    public List<ClassCommon> getClassesEu() {
        return classesEu;
    }

    public void setClassesEu(List<ClassCommon> classesEu) {
        this.classesEu = classesEu;
    }

}
