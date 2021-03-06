/*******************************************************************************
 *Copyright (c) 2009 Eucalyptus Systems, Inc.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 * 
 * 
 * This file is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Please contact Eucalyptus Systems, Inc., 130 Castilian
 * Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 * if you need additional information or have any questions.
 * 
 * This file may incorporate work covered under the following copyright and
 * permission notice:
 * 
 * Software License Agreement (BSD License)
 * 
 * Copyright (c) 2008, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use of this software in source and binary forms, with
 * or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 * THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 * LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 * SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 * IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 * BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 * THE REGENTS’ DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 * OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 * WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 * ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
/*
 * Author: chris grzegorczyk <grze@eucalyptus.com>
 */
package com.eucalyptus.bootstrap;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;
import com.eucalyptus.component.Components;
import com.eucalyptus.component.Service;
import com.eucalyptus.util.NetworkUtil;

@Deprecated
public enum Component {
  bootstrap( false, true, false ),
  component( false, true, false ),
  eucalyptus( true, false, true ),
  walrus( true, false, false ),
  dns( true, false, true ),
  storage( true, false, false ),
  db( false, false, true ),
  jetty( true, false, true ),
  configuration( true, false, true ),
  cluster( false, false, false ),
  any( false, true, false );
  private static Logger LOG = Logger.getLogger( Component.class );

  /**
   * @note is a sub-service of {@link Component.eucalyptus}
   */
  private final Boolean cloudLocal;
  /**
   * @note should have a dispatcher built
   */
  private final Boolean hasDispatcher;
  /**
   * @note always load the service locally
   */
  private final Boolean alwaysLocal;
  
  private Component( Boolean hasDispatcher, Boolean alwaysLocal, Boolean cloudLocal ) {
    this.alwaysLocal = alwaysLocal;
    this.hasDispatcher = hasDispatcher;
    this.cloudLocal = cloudLocal;
  }
  
  public Boolean isEnabled( ) {
    try {
      return Components.lookup( this ).isEnabled( );
    } catch ( NoSuchElementException ex ) {
      return false;
    }
  }
  
  public Boolean isLocal( ) {
    try {
      return Components.lookup( this ).isLocal( );
    } catch ( NoSuchElementException ex ) {
      return false;
    }
  }
  
  public String getLocalAddress( ) {
    return this.getLocalUri( ).toASCIIString( );
  }
  
  public URI getUri( ) {
    com.eucalyptus.component.Component c = Components.lookup( this );
    NavigableSet<Service> services = c.getServices( );
    if( this.isCloudLocal( ) && services.size( ) != 1 ) {
        throw new RuntimeException( "Cloud local component has "+services.size()+" registered services (Should be exactly 1): " + this + " " + services.toString( ) );
    } else if( this.isCloudLocal( ) && services.size( ) == 1 ) {
      return services.first( ).getUri( );
    } else {
      for( Service s : services ) {
        if( s.isLocal( ) ) {
          return s.getUri( );
        }
      }
      throw new RuntimeException( "Attempting to get the URI for a service which is either not a singleton or has no locally defined service endpoint." );
    }
  }

  public URI getLocalUri( ) {
    return Components.lookup( this ).getConfiguration( ).getLocalUri( );
  }
  
  public Boolean isCloudLocal( ) {
    return this.cloudLocal;
  }

  public Boolean hasDispatcher( ) {
    return this.hasDispatcher;
  }

  public Boolean isAlwaysLocal( ) {
    return this.alwaysLocal;
  }

  public static List<Component> list( ) {
    return Arrays.asList( Component.values( ) );
  }

  public String getRegistryKey( String hostName ) {
    if( NetworkUtil.testLocal( hostName ) ) {
      return this.name( ) + "@localhost";
    } else {
      return this.name( ) + "@" + hostName;
    }
  }
  
}
