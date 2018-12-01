package org.apache.tamaya.doc;

import org.apache.tamaya.doc.formats.HtmlDocFormat;
import org.apache.tamaya.doc.formats.TextDocFormat;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigDocumenterTest {

    @Test
    public void getDocumentationAndPrint_ConfigBean() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readClasses(AnnotatedDocConfigBean.class);
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertNotNull(documentation);
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_AnnotationType() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readClasses(AnnotBasedStandaloneConfigDocumentation.class);
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertNotNull(documentation);
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_Package() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readPackages("org.apache.tamaya.doc");
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertNotNull(documentation);
        System.out.println(new TextDocFormat().apply(documentation));
    }

    @Test
    public void getDocumentationAndPrint_Package_html() {
        ConfigDocumenter reader = new ConfigDocumenter();
        reader.readPackages("org.apache.tamaya.doc");
        DocumentedConfiguration documentation = reader.getDocumentation();
        assertNotNull(documentation);
        System.out.println(new HtmlDocFormat().apply(documentation));
    }
}