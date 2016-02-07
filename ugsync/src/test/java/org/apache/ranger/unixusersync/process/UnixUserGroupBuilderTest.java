/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ranger.unixusersync.process;

import org.apache.ranger.unixusersync.config.UserGroupSyncConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class UnixUserGroupBuilderTest {
    private UserGroupSyncConfig config;

    @Before
    public void setUp() throws Exception {
        config = UserGroupSyncConfig.getInstance();
        config.setProperty("ranger.usersync.unix.minUserId", "0");
        config.setProperty("ranger.usersync.unix.minGroupId", "0");
    }

    @Test
    public void testBuilderPasswd() throws Throwable {
        config.setProperty("ranger.usersync.unix.backend", "passwd");

        UnixUserGroupBuilder builder = new UnixUserGroupBuilder();
        builder.init();

        Map<String, String> groups = builder.getGroupId2groupNameMap();
        String name = groups.get("0");
        assertThat(name, anyOf(equalTo("wheel"), equalTo("root")));

        Map<String, List<String>> users = builder.getUser2GroupListMap();
        List<String> usergroups = users.get("root");
        assertNotNull(usergroups);
        assertThat(usergroups, anyOf(hasItem("wheel"), hasItem("root")));

    }

    @Test
    public void testBuilderNss() throws Throwable {
        config.setProperty("ranger.usersync.unix.backend", "nss");

        UnixUserGroupBuilder builder = new UnixUserGroupBuilder();
        builder.init();

        Map<String, String> groups = builder.getGroupId2groupNameMap();
        String name = groups.get("0");
        assertThat(name, anyOf(equalTo("wheel"), equalTo("root")));

        Map<String, List<String>> users = builder.getUser2GroupListMap();
        List<String> usergroups = users.get("root");
        assertNotNull(usergroups);
        assertThat(usergroups, anyOf(hasItem("wheel"), hasItem("root")));
    }

    @Test
    public void testBuilderExtraGroups() throws Throwable {
        config.setProperty("ranger.usersync.unix.backend", "nss");
        config.setProperty("ranger.usersync.group.enumerategroup", "root,wheel,daemon");

        UnixUserGroupBuilder builder = new UnixUserGroupBuilder();
        builder.init();

        // this is not a full test as it cannot be mocked sufficiently
        Map<String, String> groups = builder.getGroupId2groupNameMap();
        assertTrue(groups.containsValue("daemon"));
        assertThat(groups, anyOf(hasValue("wheel"), hasValue("root")));
    }

    @Test
    public void testMinUidGid() throws Throwable {
        config.setProperty("ranger.usersync.unix.backend", "nss");
        config.setProperty("ranger.usersync.unix.minUserId", "500");
        config.setProperty("ranger.usersync.unix.minGroupId", "500");

        UnixUserGroupBuilder builder = new UnixUserGroupBuilder();
        builder.init();

        // this is not a full test as it cannot be mocked sufficiently
        Map<String, String> groups = builder.getGroupId2groupNameMap();
        assertFalse(groups.containsValue("wheel"));

        Map<String, List<String>> users = builder.getUser2GroupListMap();
        assertNull(users.get("root"));
    }

}
