/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.channel;

import java.util.Map;
import java.util.Map.Entry;

import org.jboss.netty.buffer.ChannelBufferFactory;
import org.jboss.netty.buffer.HeapChannelBufferFactory;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.SocketChannelConfig;
import org.jboss.netty.handler.timeout.WriteTimeoutHandler;
import org.jboss.netty.util.internal.ConversionUtil;

/**
 * The default {@link SocketChannelConfig} implementation.
 * 
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (tlee@redhat.com)
 * 
 * @version $Rev: 1685 $, $Date: 2009-08-28 03:15:49 -0400 (Fri, 28 Aug 2009) $
 * 
 */
public class DefaultChannelConfig implements ChannelConfig {

  private volatile ChannelBufferFactory bufferFactory        = HeapChannelBufferFactory.getInstance( );
  private volatile int                  connectTimeoutMillis = 5000;                                  
  /**
   * Creates a new instance.
   */
  public DefaultChannelConfig( ) {
    super( );
  }

  public void setOptions( Map<String, Object> options ) {
    for ( Entry<String, Object> e : options.entrySet( ) ) {
      setOption( e.getKey( ), e.getValue( ) );
    }
  }

  public boolean setOption( String key, Object value ) {
    if( key == null ){
      return false;
    } else if ( key.equals( "pipelineFactory" ) ) {
      setPipelineFactory( ( ChannelPipelineFactory ) value );
    } else if ( key.equals( "connectTimeoutMillis" ) ) {
      setConnectTimeoutMillis( ConversionUtil.toInt( value ) );
    } else if ( key.equals( "bufferFactory" ) ) {
      setBufferFactory( ( ChannelBufferFactory ) value );
    } else {
      return false;
    }
    return true;
  }

  public int getConnectTimeoutMillis( ) {
    return connectTimeoutMillis;
  }

  public ChannelBufferFactory getBufferFactory( ) {
    return bufferFactory;
  }

  public void setBufferFactory( ChannelBufferFactory bufferFactory ) {
    if ( bufferFactory == null ) {
      throw new NullPointerException( "bufferFactory" );
    }
    this.bufferFactory = bufferFactory;
  }

  public ChannelPipelineFactory getPipelineFactory( ) {
    return null;
  }

  /**
   * @deprecated Use {@link WriteTimeoutHandler} instead.
   */
  @Deprecated
  public int getWriteTimeoutMillis( ) {
    return 0;
  }

  public void setConnectTimeoutMillis( int connectTimeoutMillis ) {
    if ( connectTimeoutMillis < 0 ) {
      throw new IllegalArgumentException( "connectTimeoutMillis: " + connectTimeoutMillis );
    }
    this.connectTimeoutMillis = connectTimeoutMillis;
  }

  public void setPipelineFactory( ChannelPipelineFactory pipelineFactory ) {}

  /**
   * @deprecated Use {@link WriteTimeoutHandler} instead.
   */
  @Deprecated
  public void setWriteTimeoutMillis( int writeTimeoutMillis ) {}
}