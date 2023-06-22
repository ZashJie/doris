-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.
create database test;
use test;
CREATE TABLE user_basic (
    user_id int(11) NULL COMMENT "用户ID",
    user_name varchar(20) NULL COMMENT "用户姓名",
    user_msg varchar(20) NULL COMMENT "用户信息"
) ENGINE=OLAP
DUPLICATE KEY(user_id)
COMMENT "OLAP"
DISTRIBUTED BY HASH(user_id) BUCKETS 3
PROPERTIES("replication_num" = "1");
