// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

#pragma once

#include <gutil/macros.h>

#include <string>
#include <vector>

namespace doris {

class CIDR;

class BackendOptions {
public:
    static bool init();
    static std::string get_localhost();

private:
    static bool analyze_network_interfaces();
    static bool analyze_priority_cidrs();
    static bool is_in_prior_network(const std::string& ip);
    static bool is_in_network_interface(const std::string& ip);

    static std::vector<std::string> _s_network_interfaces;
    static std::string _s_localhost;
    static std::vector<CIDR> _s_priority_cidrs;

    DISALLOW_COPY_AND_ASSIGN(BackendOptions);
};

} // namespace doris
