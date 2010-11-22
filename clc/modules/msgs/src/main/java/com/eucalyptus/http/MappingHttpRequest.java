/*******************************************************************************
*Copyright (c) 2009  Eucalyptus Systems, Inc.
* 
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, only version 3 of the License.
* 
* 
*  This file is distributed in the hope that it will be useful, but WITHOUT
*  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
*  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
* 
*  You should have received a copy of the GNU General Public License along
*  with this program.  If not, see <http://www.gnu.org/licenses/>.
* 
*  Please contact Eucalyptus Systems, Inc., 130 Castilian
*  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
*  if you need additional information or have any questions.
* 
*  This file may incorporate work covered under the following copyright and
*  permission notice:
* 
*    Software License Agreement (BSD License)
* 
*    Copyright (c) 2008, Regents of the University of California
*    All rights reserved.
* 
*    Redistribution and use of this software in source and binary forms, with
*    or without modification, are permitted provided that the following
*    conditions are met:
* 
*      Redistributions of source code must retain the above copyright notice,
*      this list of conditions and the following disclaimer.
* 
*      Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
* 
*    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
*    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
*    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
*    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
*    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
*    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
*    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
*    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
*    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
*    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
*    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
*    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
*    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
*    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
*    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
*    THE REGENTS’ DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
*    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
*    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
*    ANY SUCH LICENSES OR RIGHTS.
*******************************************************************************/
/*
 * Author: chris grzegorczyk <grze@eucalyptus.com>
 */
package com.eucalyptus.http;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import com.eucalyptus.component.ServiceEndpoint;
import edu.ucsb.eucalyptus.msgs.BaseMessage;

public class MappingHttpRequest extends MappingHttpMessage implements HttpRequest {
  
  private final HttpMethod method;
  private final String     uri;
  private String     servicePath;
  private String     query;
  private final Map<String,String> parameters;
  private String restNamespace;
  private final Map formFields;
  
  public MappingHttpRequest( HttpVersion httpVersion, HttpMethod method, String uri ) {
    super( httpVersion );
    this.method = method;
    this.uri = uri;
    try {
      URL url = new URL( "http://eucalyptus" + uri );
      this.servicePath = url.getPath( );
      this.parameters = new HashMap<String, String>( );
      this.query = this.query == url.getQuery( ) ? this.query : url.getQuery( );// new URLCodec().decode(url.toURI( ).getQuery( ) ).replaceAll( " ", "+" );
      this.formFields = new HashMap<String, String>( );
      this.populateParameters();
    } catch ( MalformedURLException e ) {
      throw new RuntimeException( e );
    } 
  }

  private void populateParameters( ) {
    if ( this.query != null && !"".equals(  this.query ) ) {
      for ( String p : this.query.split( "&" ) ) {
        String[] splitParam = p.split( "=" );
        String lhs = splitParam[0];
        String rhs = splitParam.length == 2 ? splitParam[1] : null;
        try {
          if( lhs != null ) lhs = new URLCodec().decode(lhs);
        } catch ( DecoderException e ) {}
        try {
          if( rhs != null ) rhs = new URLCodec().decode(rhs);
        } catch ( DecoderException e ) {}
        this.parameters.put( lhs, rhs );
      }
    }
  }
  
  public MappingHttpRequest( final HttpVersion httpVersion, final HttpMethod method, final String host, final int port, final String servicePath,
                             final Object source ) {
    super( httpVersion );
    this.method = method;
    this.uri = "http://" + host + ":" + port + servicePath;
    this.servicePath = servicePath;
    this.query = null;
    this.parameters = null;
    this.formFields = null;
    super.setMessage( source );
    this.addHeader( HttpHeaders.Names.HOST, host + ":" + port );
  }
  
  @Override
  public void setMessage( Object message ) {
    if ( message instanceof BaseMessage ) {
      ( ( BaseMessage ) message ).setCorrelationId( this.getCorrelationId( ) );
    }
    super.setMessage( message );
  }
  
  public String getServicePath( ) {
    return this.servicePath;
  }
  
  public void setServicePath( String servicePath ) {
    this.servicePath = servicePath;
  }

  
  public String getQuery( ) {
    return this.query;
  }
  
  public void setQuery( String query ) {
    try {
      this.query = new URLCodec( ).decode( query );
    } catch ( DecoderException e ) {
      this.query = query;
    }
    this.populateParameters( );
  }


  public HttpMethod getMethod( ) {
    return this.method;
  }

  public String getUri( ) {
    return this.uri;
  }

  @Override
  public String toString( ) {
    return this.getMethod( ).toString( ) + ' ' + this.getUri( ) + ' ' + super.getProtocolVersion( ).getText( );
  }
  
  public Map<String, String> getParameters( ) {
    return parameters;
  }

  public String getRestNamespace( ) {
    return restNamespace;
  }

  public void setRestNamespace( String restNamespace ) {
    this.restNamespace = restNamespace;
  }
  
  public Map getFormFields( ) {
    return formFields;
  }
  
  public String getAndRemoveHeader( String key ) {
    String value = getHeader( key );
    removeHeader( key );
    return value;
  }
}
