// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
///////////////////////////////////////////////////////////////////////////////

#ifndef TINK_CLEARTEXT_KEYSET_HANDLE_H_
#define TINK_CLEARTEXT_KEYSET_HANDLE_H_

#include <istream>
#include <sstream>

#include "cc/keyset_handle.h"
#include "cc/util/statusor.h"

namespace crypto {
namespace tink {

// Creates keyset handles from cleartext keysets. This API allows
// loading cleartext keysets, thus its usage should be restricted.
class CleartextKeysetHandle {
 public:
  static util::StatusOr<std::unique_ptr<KeysetHandle>> ParseFrom(
      const std::string& serialized_keyset);
  static util::StatusOr<std::unique_ptr<KeysetHandle>> ParseFrom(
      std::istream* keyset_stream);
 private:
  CleartextKeysetHandle() {};
};

}  // namespace tink
}  // namespace crypto

#endif  // TINK_CLEARTEXT_KEYSET_HANDLE_H_
