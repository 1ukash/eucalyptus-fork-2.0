/*
Copyright (c) 2009  Eucalyptus Systems, Inc.	

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by 
the Free Software Foundation, only version 3 of the License.  
 
This file is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.  

You should have received a copy of the GNU General Public License along
with this program.  If not, see <http://www.gnu.org/licenses/>.
 
Please contact Eucalyptus Systems, Inc., 130 Castilian
Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/> 
if you need additional information or have any questions.

This file may incorporate work covered under the following copyright and
permission notice:

  Software License Agreement (BSD License)

  Copyright (c) 2008, Regents of the University of California
  

  Redistribution and use of this software in source and binary forms, with
  or without modification, are permitted provided that the following
  conditions are met:

    Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

    Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
  THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
  LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
  SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
  IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
  BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
  THE REGENTS’ DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
  OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
  WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
  ANY SUCH LICENSES OR RIGHTS.
*/
#include <stdio.h>
#include <stdlib.h>
#include <gl-client-marshal.h>
#include <euca_auth.h>

int main(int argc, char **argv) {
  axutil_env_t * env = NULL;
  axis2_char_t * client_home = NULL;
  axis2_char_t endpoint_uri[256], *tmpstr;
  axis2_stub_t * stub = NULL;
  int rc, i;
  char *euca_home;
  
  snprintf(endpoint_uri, 256," http://%s/axis2/services/EucalyptusGL", argv[1]);
  //  env =  axutil_env_create_all("/tmp/GLclient.log", AXIS2_LOG_LEVEL_TRACE);
  env =  axutil_env_create_all("/tmp/fooh", AXIS2_LOG_LEVEL_TRACE);
  client_home = AXIS2_GETENV("AXIS2C_HOME");
  if (!client_home) {
    printf("must have AXIS2C_HOME set\n");
  }
  stub = axis2_stub_create_EucalyptusGL(env, client_home, endpoint_uri);

  if (!strcmp(argv[2], "getLogs")) {
    char *clog, *nlog, *hlog, *alog;
    rc = gl_getLogs(argv[3], &clog, &nlog, &hlog, &alog, env, stub);
    if (!rc) {
      if (clog) printf("CLOG\n----------\n%s\n-----------\n", base64_dec((unsigned char *)clog, strlen(clog)));
      if (nlog) printf("NLOG\n----------\n%s\n-----------\n", base64_dec((unsigned char *)nlog, strlen(nlog)));
      if (hlog) printf("HLOG\n----------\n%s\n-----------\n", base64_dec((unsigned char *)hlog, strlen(hlog)));
      if (alog) printf("ALOG\n----------\n%s\n-----------\n", base64_dec((unsigned char *)alog, strlen(alog)));
    }
  } else if (!strcmp(argv[2], "getKeys")) {
    char *cccert, *nccert;
    rc = gl_getKeys(argv[3], &cccert, &nccert, env, stub);
    if (!rc) {
      if (cccert) printf("CCCERT\n----------\n%s\n-----------\n", base64_dec((unsigned char *)cccert, strlen(cccert)));
      if (nccert) printf("NCCERT\n----------\n%s\n-----------\n", base64_dec((unsigned char *)nccert, strlen(nccert)));
    }
  }
  exit(0);
}
