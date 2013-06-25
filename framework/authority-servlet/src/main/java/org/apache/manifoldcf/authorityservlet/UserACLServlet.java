/* $Id: UserACLServlet.java 988245 2010-08-23 18:39:35Z kwright $ */

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
package org.apache.manifoldcf.authorityservlet;

import org.apache.manifoldcf.core.interfaces.*;
import org.apache.manifoldcf.authorities.interfaces.*;
import org.apache.manifoldcf.authorities.system.ManifoldCF;
import org.apache.manifoldcf.authorities.system.Logging;
import org.apache.manifoldcf.authorities.system.RequestQueue;
import org.apache.manifoldcf.authorities.system.AuthRequest;
import org.apache.manifoldcf.authorities.system.MappingRequest;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/** This servlet class is meant to receive a user name and return a list of access tokens.
* The user name is expected to be sent as an argument on the url (the "username" argument), and the
* response will simply be a list of access tokens separated by newlines.
* This is guaranteed safe because the index system cannot work with access tokens that aren't 7-bit ascii that
* have any control characters in them.
*
* Errors will simply report back with an empty acl.
*
* The content type will always be text/plain.
*/
public class UserACLServlet extends HttpServlet
{
  public static final String _rcsid = "@(#)$Id: UserACLServlet.java 988245 2010-08-23 18:39:35Z kwright $";

  protected final static String AUTHORIZED_VALUE = "AUTHORIZED:";
  protected final static String UNREACHABLE_VALUE = "UNREACHABLEAUTHORITY:";
  protected final static String UNAUTHORIZED_VALUE = "UNAUTHORIZED:";
  protected final static String USERNOTFOUND_VALUE = "USERNOTFOUND:";

  protected final static String ID_PREFIX = "ID:";
  protected final static String TOKEN_PREFIX = "TOKEN:";

  /** The init method.
  */
  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    try
    {
      // Set up the environment
      //ManifoldCF.initializeEnvironment();
      IThreadContext itc = ThreadContextFactory.make();
      ManifoldCF.startSystem(itc);
    }
    catch (ManifoldCFException e)
    {
      Logging.misc.error("Error starting authority service: "+e.getMessage(),e);
      throw new ServletException("Error starting authority service: "+e.getMessage(),e);
    }

  }

  /** The destroy method.
  */
  public void destroy()
  {
    try
    {
      // Set up the environment
      //ManifoldCF.initializeEnvironment();
      IThreadContext itc = ThreadContextFactory.make();
      ManifoldCF.stopSystem(itc);
    }
    catch (ManifoldCFException e)
    {
      Logging.misc.error("Error shutting down authority service: "+e.getMessage(),e);
    }
    super.destroy();
  }

  /** The get method.
  */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    try
    {
      // Set up the environment
      //ManifoldCF.initializeEnvironment();

      Logging.authorityService.debug("Received request");

      String userID = request.getParameter("username");
      if (userID == null)
      {
        response.sendError(response.SC_BAD_REQUEST);
        return;
      }

      String[] domains = request.getParameterValues("domain");
      
      UserRecord userRecord = new UserRecord();

      if (domains == null)
      {
        int atIndex = userID.indexOf("@");
        if (atIndex == -1)
          userRecord.setDomainValue(userRecord.DOMAIN_ACTIVEDIRECTORY, userID);
        else
        {
          UserRecord u2 = new UserRecord();
          u2.setDomainValue(userID.substring(atIndex+1), userID.substring(0,atIndex));
          userRecord.setDomainValue(userRecord.DOMAIN_ACTIVEDIRECTORY, u2);
        }
      }
      else
      {
        int domainIndex = domains.length;
        while (--domainIndex >= 0)
        {
          if (domainIndex == domains.length-1)
          {
            userRecord.setDomainValue(domains[domainIndex], userID);
          }
          else
          {
            UserRecord newUserRecord = new UserRecord();
            newUserRecord.setDomainValue(domains[domainIndex], userRecord);
            userRecord = newUserRecord;
          }
        }
      }

      boolean idneeded = false;
      boolean aclneeded = true;

      String idneededValue = request.getParameter("idneeded");
      if (idneededValue != null)
      {
        if (idneededValue.equals("true"))
          idneeded = true;
        else if (idneededValue.equals("false"))
          idneeded = false;
      }
      String aclneededValue = request.getParameter("aclneeded");
      if (aclneededValue != null)
      {
        if (aclneededValue.equals("true"))
          aclneeded = true;
        else if (aclneededValue.equals("false"))
          aclneeded = false;
      }

      if (Logging.authorityService.isDebugEnabled())
      {
        Logging.authorityService.debug("Received authority request for user '"+userRecord.toString()+"'");
      }

      RequestQueue<MappingRequest> mappingQueue = ManifoldCF.getMappingRequestQueue();
      if (mappingQueue == null)
      {
        // System wasn't started; return unauthorized
        throw new ManifoldCFException("System improperly initialized");
      }

      RequestQueue<AuthRequest> queue = ManifoldCF.getRequestQueue();
      if (queue == null)
      {
        // System wasn't started; return unauthorized
        throw new ManifoldCFException("System improperly initialized");
      }

      
      IThreadContext itc = ThreadContextFactory.make();
      
      IMappingConnectionManager mappingConnManager = MappingConnectionManagerFactory.make(itc);
      
      IMappingConnection[] mappingConnections = mappingConnManager.getAllConnections();
      // One thread per connection, which is responsible for starting the mapping process when it is ready.
      MappingOrderThread[] mappingThreads = new MappingOrderThread[mappingConnections.length];
      // Requests that exist but may not yet have been queued
      Map<String,MappingRequest> mappingRequests = new HashMap<String,MappingRequest>();
      
      for (int i = 0; i < mappingConnections.length; i++)
      {
        IMappingConnection thisConnection = mappingConnections[i];
        String identifyingString = thisConnection.getDescription();
        
        // Create a record and add it to the queue
        MappingRequest mr = new MappingRequest(userRecord,
          thisConnection.getClassName(),identifyingString,thisConnection.getConfigParams(),thisConnection.getMaxConnections());
        
        mappingRequests.put(thisConnection.getName(), mr);
        mappingThreads[i] = new MappingOrderThread(mappingQueue, mappingRequests, thisConnection);
      }
      
      // Start the threads!
      for (int i = 0; i < mappingConnections.length; i++)
      {
        mappingThreads[i].start();
      }
      
      // Wait for the threads to finish up.  This will guarantee that all mappers have been started.
      for (int i = 0;  i < mappingConnections.length; i++)
      {
        mappingThreads[i].finishUp();
      }
      
      // Wait for everything to finish.
      for (MappingRequest mr : mappingRequests.values())
      {
        mr.waitForComplete();
      }
      
      // Handle all exceptions thrown during mapping.  In general this just means logging them, because
      // the downstream authorities will presumably not find what they are looking for and error out that way.
      for (MappingRequest mr : mappingRequests.values())
      {
        Throwable exception = mr.getAnswerException();
        if (exception != null)
        {
          Logging.authorityService.warn("Mapping exception logged from "+mr.getIdentifyingString()+": "+exception.getMessage()+"; mapper aborted", exception);
        }
      }
      
      IAuthorityConnectionManager authConnManager = AuthorityConnectionManagerFactory.make(itc);

      IAuthorityConnection[] connections = authConnManager.getAllConnections();
      
      AuthRequest[] requests = new AuthRequest[connections.length];

      // Queue up all the requests
      for (int i = 0; i < connections.length; i++)
      {
        IAuthorityConnection ac = connections[i];

        String identifyingString = ac.getDescription();
        if (identifyingString == null || identifyingString.length() == 0)
          identifyingString = ac.getName();

        AuthRequest ar = new AuthRequest(userRecord,ac.getClassName(),identifyingString,ac.getConfigParams(),ac.getMaxConnections());
        queue.addRequest(ar);

        requests[i] = ar;
      }

      // Now, work through the returning answers.

      // Ask all the registered authorities for their ACLs, and merge the final list together.
      StringBuilder sb = new StringBuilder();
      // Set response mime type
      response.setContentType("text/plain; charset=ISO8859-1");
      ServletOutputStream out = response.getOutputStream();
      try
      {
        for (int i = 0; i < connections.length; i++)
        {
          IAuthorityConnection ac = connections[i];
          AuthRequest ar = requests[i];

          if (Logging.authorityService.isDebugEnabled())
            Logging.authorityService.debug("Waiting for answer from connector class '"+ac.getClassName()+"' for user '"+userRecord.toString()+"'");

          ar.waitForComplete();

          if (Logging.authorityService.isDebugEnabled())
            Logging.authorityService.debug("Received answer from connector class '"+ac.getClassName()+"' for user '"+userRecord.toString()+"'");

          Throwable exception = ar.getAnswerException();
          AuthorizationResponse reply = ar.getAnswerResponse();
          if (exception != null)
          {
            // Exceptions are always bad now
            // The ManifoldCFException here must disable access to the UI without causing a generic badness thing to happen, so use 403.
            if (exception instanceof ManifoldCFException)
              response.sendError(response.SC_FORBIDDEN,"From "+ar.getIdentifyingString()+": "+exception.getMessage());
            else
              response.sendError(response.SC_INTERNAL_SERVER_ERROR,"From "+ar.getIdentifyingString()+": "+exception.getMessage());
            return;
          }

          if (reply.getResponseStatus() == AuthorizationResponse.RESPONSE_UNREACHABLE)
          {
            Logging.authorityService.warn("Authority '"+ar.getIdentifyingString()+"' is unreachable for user '"+userID+"'");
            sb.append(UNREACHABLE_VALUE).append(java.net.URLEncoder.encode(ar.getIdentifyingString(),"UTF-8")).append("\n");
          }
          else if (reply.getResponseStatus() == AuthorizationResponse.RESPONSE_USERUNAUTHORIZED)
          {
            if (Logging.authorityService.isDebugEnabled())
              Logging.authorityService.debug("Authority '"+ar.getIdentifyingString()+"' does not authorize user '"+userRecord.toString()+"'");
            sb.append(UNAUTHORIZED_VALUE).append(java.net.URLEncoder.encode(ar.getIdentifyingString(),"UTF-8")).append("\n");
          }
          else if (reply.getResponseStatus() == AuthorizationResponse.RESPONSE_USERNOTFOUND)
          {
            if (Logging.authorityService.isDebugEnabled())
              Logging.authorityService.debug("User '"+userRecord.toString()+"' unknown to authority '"+ar.getIdentifyingString()+"'");
            sb.append(USERNOTFOUND_VALUE).append(java.net.URLEncoder.encode(ar.getIdentifyingString(),"UTF-8")).append("\n");
          }
          else
            sb.append(AUTHORIZED_VALUE).append(java.net.URLEncoder.encode(ar.getIdentifyingString(),"UTF-8")).append("\n");

          String[] acl = reply.getAccessTokens();
          if (acl != null)
          {
            if (aclneeded)
            {
              int j = 0;
              while (j < acl.length)
              {
                if (Logging.authorityService.isDebugEnabled())
                  Logging.authorityService.debug("  User '"+userRecord.toString()+"' has Acl = '"+acl[j]+"' from authority '"+ar.getIdentifyingString()+"'");
                sb.append(TOKEN_PREFIX).append(java.net.URLEncoder.encode(ac.getName(),"UTF-8")).append(":").append(java.net.URLEncoder.encode(acl[j++],"UTF-8")).append("\n");
              }
            }
          }
        }

        if (idneeded)
          sb.append(ID_PREFIX).append(java.net.URLEncoder.encode(userID,"UTF-8")).append("\n");

        byte[] responseValue = sb.toString().getBytes("ISO8859-1");

        response.setIntHeader("Content-Length", (int)responseValue.length);
        out.write(responseValue,0,responseValue.length);
        out.flush();
      }
      finally
      {
        out.close();
      }

      if (Logging.authorityService.isDebugEnabled())
        Logging.authorityService.debug("Done with request for '"+userRecord.toString()+"'");
    }
    catch (InterruptedException e)
    {
      // Shut down and don't bother to respond
    }
    catch (java.io.UnsupportedEncodingException e)
    {
      Logging.authorityService.error("Unsupported encoding: "+e.getMessage(),e);
      throw new ServletException("Fatal error occurred: "+e.getMessage(),e);
    }
    catch (ManifoldCFException e)
    {
      Logging.authorityService.error("User ACL servlet error: "+e.getMessage(),e);
      response.sendError(response.SC_INTERNAL_SERVER_ERROR,e.getMessage());
    }
  }

  /** This thread is responsible for making sure that the constraints for a given mapping connection
  * are met, and then when they are, firing off a MappingRequest.  One of these threads is spun up
  * for every IMappingConnection being handled.
  * NOTE WELL: The number of threads this might require is worrisome.  It is essentially
  * <number_of_app_server_threads> * <number_of_mappers>.  I will try later to see if I can find
  * a way of limiting this to sane numbers.
  */
  protected static class MappingOrderThread extends Thread
  {
    protected final Map<String,MappingRequest> requests;
    protected final RequestQueue<MappingRequest> mappingRequestQueue;
    protected final IMappingConnection mappingConnection;
    
    protected Throwable exception = null;
    
    public MappingOrderThread(RequestQueue<MappingRequest> mappingRequestQueue,
      Map<String, MappingRequest> requests,
      IMappingConnection mappingConnection)
    {
      super();
      this.mappingRequestQueue = mappingRequestQueue;
      this.mappingConnection = mappingConnection;
      this.requests = requests;
      setName("Constraint matcher for mapper "+mappingConnection.getName());
      setDaemon(true);
    }
    
    public void run()
    {
      try
      {
        while (true)
        {
          Set<String> prereqs = mappingConnection.getPrerequisites();
          for (String x : prereqs)
          {
            MappingRequest mr = requests.get(x);
            mr.waitForComplete();
          }
          // Constraints are met.  Fire off the request.
          mappingRequestQueue.addRequest(requests.get(mappingConnection.getName()));
        }
      }
      catch (Throwable e)
      {
        exception = e;
      }
    }

    public void finishUp()
      throws InterruptedException
    {
      join();
      if (exception != null)
      {
        if (exception instanceof Error)
          throw (Error)exception;
        else if (exception instanceof RuntimeException)
          throw (RuntimeException)exception;
      }
    }
    
  }
  
}
