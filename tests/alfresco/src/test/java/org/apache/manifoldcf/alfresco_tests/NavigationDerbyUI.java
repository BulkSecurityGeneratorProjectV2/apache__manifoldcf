/* $Id$ */

/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.manifoldcf.alfresco_tests;

import java.util.Locale;

import org.apache.manifoldcf.core.tests.HTMLTester;
import org.junit.Test;

/** Basic UI navigation tests */
public class NavigationDerbyUI extends BaseUIDerby
{

  @Test
  public void createConnectionsAndJob()
    throws Exception
  {
    testerInstance.newTest(Locale.US);
    
    HTMLTester.Window window;
    HTMLTester.Link link;
    HTMLTester.Form form;
    HTMLTester.Textarea textarea;
    HTMLTester.Selectbox selectbox;
    HTMLTester.Button button;
    HTMLTester.Radiobutton radiobutton;
    HTMLTester.Loop loop;
    
    window = testerInstance.openMainWindow("http://localhost:8346/mcf-crawler-ui/index.jsp");
    
    // Define an output connection via the UI
    link = window.findLink(testerInstance.createStringDescription("List output connections"));
    link.click();
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Add an output connection"));
    link.click();
    // Fill in a name
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editconnection"));
    textarea = form.findTextarea(testerInstance.createStringDescription("connname"));
    textarea.setValue(testerInstance.createStringDescription("MyOutputConnection"));
    link = window.findLink(testerInstance.createStringDescription("Type tab"));
    link.click();
    // Select a type
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editconnection"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("classname"));
    selectbox.selectValue(testerInstance.createStringDescription("org.apache.manifoldcf.agents.output.nullconnector.NullConnector"));
    button = window.findButton(testerInstance.createStringDescription("Continue to next page"));
    button.click();
    // Visit the Throttling tab
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Throttling tab"));
    link.click();
    // Go back to the Name tab
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Name tab"));
    link.click();
    // Now save the connection.
    window = testerInstance.findWindow(null);
    button = window.findButton(testerInstance.createStringDescription("Save this output connection"));
    button.click();
    
    // Define a repository connection via the UI
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("List repository connections"));
    link.click();
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Add a connection"));
    link.click();
    // Fill in a name
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editconnection"));
    textarea = form.findTextarea(testerInstance.createStringDescription("connname"));
    textarea.setValue(testerInstance.createStringDescription("MyRepositoryConnection"));
    link = window.findLink(testerInstance.createStringDescription("Type tab"));
    link.click();
    // Select a type
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editconnection"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("classname"));
    selectbox.selectValue(testerInstance.createStringDescription("org.apache.manifoldcf.crawler.connectors.alfresco.AlfrescoRepositoryConnector"));
    button = window.findButton(testerInstance.createStringDescription("Continue to next page"));
    button.click();
    // Visit the Throttling tab
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Throttling tab"));
    link.click();
    // Server tab
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Server tab"));
    link.click();
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editconnection"));
    textarea = form.findTextarea(testerInstance.createStringDescription("username"));
    textarea.setValue(testerInstance.createStringDescription("admin"));
    textarea = form.findTextarea(testerInstance.createStringDescription("password"));
    textarea.setValue(testerInstance.createStringDescription("admin"));
    textarea = form.findTextarea(testerInstance.createStringDescription("tenantDomain"));
    textarea.setValue(testerInstance.createStringDescription(""));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("protocol"));
    selectbox.selectValue(testerInstance.createStringDescription("http"));
    textarea = form.findTextarea(testerInstance.createStringDescription("server"));
    textarea.setValue(testerInstance.createStringDescription("localhost"));
    textarea = form.findTextarea(testerInstance.createStringDescription("port"));
    textarea.setValue(testerInstance.createStringDescription("9090"));
    textarea = form.findTextarea(testerInstance.createStringDescription("path"));
    textarea.setValue(testerInstance.createStringDescription("/alfresco/api"));
    textarea = form.findTextarea(testerInstance.createStringDescription("socketTimeout"));
    textarea.setValue(testerInstance.createStringDescription("120000"));
    // Go back to the Name tab
    link = window.findLink(testerInstance.createStringDescription("Name tab"));
    link.click();
    window = testerInstance.findWindow(null);
    // Now save the connection.
    button = window.findButton(testerInstance.createStringDescription("Save this connection"));
    button.click();
    
    // Create a job
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("List jobs"));
    link.click();
    // Add a job
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Add a job"));
    link.click();
    // Fill in a name
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editjob"));
    textarea = form.findTextarea(testerInstance.createStringDescription("description"));
    textarea.setValue(testerInstance.createStringDescription("MyJob"));
    link = window.findLink(testerInstance.createStringDescription("Connection tab"));
    link.click();
    // Select the connections
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editjob"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("outputname"));
    selectbox.selectValue(testerInstance.createStringDescription("MyOutputConnection"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("connectionname"));
    selectbox.selectValue(testerInstance.createStringDescription("MyRepositoryConnection"));
    button = window.findButton(testerInstance.createStringDescription("Continue to next screen"));
    button.click();
    // Visit all the tabs.  Scheduling tab first
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Scheduling tab"));
    link.click();
    window = testerInstance.findWindow(null);
    form = window.findForm(testerInstance.createStringDescription("editjob"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("dayofweek"));
    selectbox.selectValue(testerInstance.createStringDescription("0"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("hourofday"));
    selectbox.selectValue(testerInstance.createStringDescription("1"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("minutesofhour"));
    selectbox.selectValue(testerInstance.createStringDescription("30"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("monthofyear"));
    selectbox.selectValue(testerInstance.createStringDescription("11"));
    selectbox = form.findSelectbox(testerInstance.createStringDescription("dayofmonth"));
    selectbox.selectValue(testerInstance.createStringDescription("none"));
    textarea = form.findTextarea(testerInstance.createStringDescription("duration"));
    textarea.setValue(testerInstance.createStringDescription("120"));
    button = window.findButton(testerInstance.createStringDescription("Add new schedule record"));
    button.click();
    window = testerInstance.findWindow(null);
    // MHL
    // Save the job
    button = window.findButton(testerInstance.createStringDescription("Save this job"));
    button.click();

    // Delete the job
    window = testerInstance.findWindow(null);
    HTMLTester.StringDescription jobID = window.findMatch(testerInstance.createStringDescription("<!--jobid=(.*?)-->"),0);
    testerInstance.printValue(jobID);
    link = window.findLink(testerInstance.createStringDescription("Delete this job"));
    link.click();
    
    // Wait for the job to go away
    loop = testerInstance.beginLoop(120);
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Manage jobs"));
    link.click();
    window = testerInstance.findWindow(null);
    HTMLTester.StringDescription isJobNotPresent = window.isNotPresent(jobID);
    testerInstance.printValue(isJobNotPresent);
    loop.breakWhenTrue(isJobNotPresent);
    loop.endLoop();
    
    // Delete the repository connection
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("List repository connections"));
    link.click();
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Delete MyRepositoryConnection"));
    link.click();
    
    // Delete the output connection
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("List output connections"));
    link.click();
    window = testerInstance.findWindow(null);
    link = window.findLink(testerInstance.createStringDescription("Delete MyOutputConnection"));
    link.click();
    
    testerInstance.executeTest();
  }
  
}
