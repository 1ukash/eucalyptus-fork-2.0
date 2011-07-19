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
package com.eucalyptus.ws.handlers;

import java.io.IOException;
import javax.security.auth.login.LoginException;
import java.nio.channels.ClosedChannelException;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.eucalyptus.system.LogLevels;
import com.eucalyptus.util.LogUtil;
import com.eucalyptus.ws.WebServicesException;

public abstract class MessageStackHandler implements ChannelDownstreamHandler, ChannelUpstreamHandler {
  private static Logger LOG = Logger.getLogger( MessageStackHandler.class );

  @Override
  public void handleDownstream( final ChannelHandlerContext channelHandlerContext, final ChannelEvent channelEvent ) throws Exception {
    if( LogLevels.TRACE) {
      LOG.trace( LogUtil.dumpObject( channelEvent ) );
    }
    try {
      if ( channelEvent instanceof MessageEvent ) {
        final MessageEvent msgEvent = ( MessageEvent ) channelEvent;
        if( msgEvent.getMessage() != null ) {
          this.outgoingMessage( channelHandlerContext, msgEvent );
        } else {
          LOG.warn( "==> Outbound message is null!: " + LogUtil.dumpObject( channelEvent ) );
        }
      }
    } catch ( Throwable e ) {
      LOG.debug( e, e );
      Channels.fireExceptionCaught( channelHandlerContext, e );
    }
    channelHandlerContext.sendDownstream( channelEvent );
  }

  public abstract void outgoingMessage( final ChannelHandlerContext ctx, MessageEvent event ) throws Exception;

  public abstract void incomingMessage( final ChannelHandlerContext ctx, MessageEvent event ) throws Exception;

  public void exceptionCaught( final ChannelHandlerContext ctx, final ExceptionEvent exceptionEvent ) throws Exception {//FIXME: handle exceptions cleanly.
    Throwable t = exceptionEvent.getCause( );
    if ( t != null && IOException.class.isAssignableFrom( t.getClass( ) ) ) {
      LOG.error( t, t );
    } else {
      LOG.debug( t, t );
    }
    ctx.sendUpstream( exceptionEvent );
  }

  @Override
  public void handleUpstream( final ChannelHandlerContext channelHandlerContext, final ChannelEvent channelEvent ) throws Exception {
    if( LogLevels.TRACE) {
      LOG.trace( LogUtil.dumpObject( channelEvent ) );
    }
    if ( channelEvent instanceof MessageEvent ) {
      final MessageEvent msgEvent = ( MessageEvent ) channelEvent;
      try {
        this.incomingMessage( channelHandlerContext, msgEvent );
      } catch ( LoginException e ) {                                                                                                         
	 LOG.error( e, e );                                                                                                                   
	 throw new WebServicesException( e.getMessage( ), HttpResponseStatus.FORBIDDEN );                                                     
      } catch ( Throwable e ) {
        LOG.error( e, e );
	throw new WebServicesException( e.getMessage( ), HttpResponseStatus.BAD_REQUEST );
      } 
    }
    channelHandlerContext.sendUpstream( channelEvent );
  }
}
