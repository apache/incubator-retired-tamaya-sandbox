/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy createObject the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.tamaya.doc.formats;

import j2html.tags.ContainerTag;
import org.apache.tamaya.doc.DocFormat;
import org.apache.tamaya.doc.DocumentedArea;
import org.apache.tamaya.doc.DocumentedConfiguration;
import org.apache.tamaya.doc.DocumentedProperty;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.i;
import static j2html.TagCreator.li;
import static j2html.TagCreator.link;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.table;
import static j2html.TagCreator.td;
import static j2html.TagCreator.text;
import static j2html.TagCreator.th;
import static j2html.TagCreator.title;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

/**
 * An HTML-based documentation format.
 */
public class HtmlDocFormat implements DocFormat<String> {
    @Override
    public String apply(DocumentedConfiguration documentedConfiguration) {
        List<ContainerTag> areaTags = new ArrayList<>();
        for(DocumentedArea area:documentedConfiguration.getAllAreasSorted()) {
            areaTags.addAll(createAreaEntries(area, null));
        }
        ContainerTag propertiesTable = createPropertiesTable(documentedConfiguration.getAllPropertiesSorted(), null);

        ContainerTag head = createHead(documentedConfiguration);
        ContainerTag body = body()
                .with(
                    h1("Configuration Documentation (" + documentedConfiguration.getName() + ")"),
                    p("Version: " + documentedConfiguration.getVersion()),
                    h2("Documented Areas")).with(areaTags)
                .with(
                    h2("Documented Properties"),
                    propertiesTable);
        String result = html(head, body).render();
        writeResultToFile(result);
        return result;
    }

    private void writeResultToFile(String result) {
        File file = new File("./doc.html");
        FileWriter w;
        try {
            w = new FileWriter(file);
            w.append(result);
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ContainerTag createHead(DocumentedConfiguration config) {
        return head(title("Tamaya Configuration - " + config.getName() + " " +
                        config.getVersion()),
                meta().withCharset("utf-8"),
                meta().withName("viewport").withContent("width=device-width, initial-scale=0.9, shrink-to-fit=yes"),
                link().withRel("stylesheet")
                        .withHref("https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css")
                        .attr("integrity","sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm")
                        .attr("crossorigin","anonymous"));
    }

    private ContainerTag createPropertiesTable(List<DocumentedProperty> properties, String parentArea) {
        List<ContainerTag> propertyRows = new ArrayList<>();
        for (DocumentedProperty prop : properties) {
            propertyRows.add(tr(
                    td(pre(prop.getName().replace(", ", "\n"))).attr("scope","row"),
                    td(Object.class==prop.getValueType()?"":prop.getValueType().getName()),
                    td(prop.getDescription()),
                    td(i(prop.getDefaultValue()))
            ));
        }
        ContainerTag propertiesTable = table(
                thead(tr(
                        th("Key(s)").attr("scope","col"),
                        th("Valuetype").attr("scope","col"),
                        th("Description").attr("width", "75%").attr("scope","col"),
                        th("Default").attr("scope","col").attr("width", "15%")
                )).withClass("thead-dark")
        ).with(propertyRows.toArray(new ContainerTag[propertyRows.size()]))
                .withClass("table table-striped table-hover table-bordered table-sm")
                .attr("width", "90%");
        return propertiesTable;
    }

    private List<ContainerTag> createAreaEntries(DocumentedArea area, String parentArea) {
        List<ContainerTag> result = new ArrayList<>();
        if(parentArea==null){
            result.add(h4(area.getPath()));
        }else{
            result.add(h4(parentArea + "."+ area.getPath()));
        }
        result.add(ul(
                li(b("Group Type: "), text(area.getGroupType().toString()),
                li(b("Valuetype: "), text(area.getValueType().getName()))
                        .withCondHidden(Object.class==area.getValueType()),
                li(b("Description: "), text(area.getDescription()))
                        .withCondHidden(area.getDescription()==null),
                li(b("Properties: ")).with(createPropertiesTable(area.getPropertiesSorted(), area.getPath()))
                        .withCondHidden(area.getProperties().isEmpty())
        )));
        for(DocumentedArea subArea:area.getAreasSorted()){
            result.addAll(createAreaEntries(subArea, area.getPath()));
        }
        return result;
    }

}
