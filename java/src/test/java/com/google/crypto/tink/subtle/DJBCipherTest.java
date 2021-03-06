// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////

package com.google.crypto.tink.subtle;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.truth.Truth;
import com.google.crypto.tink.subtle.DJBCipher.StateGen;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link DJBCipher}.
 */
@RunWith(JUnit4.class)
public class DJBCipherTest {

  class MockDJBCipher extends DJBCipher {

    public MockDJBCipher(byte[] key) {
      super(key);
    }

    @Override
    void shuffle(int[] state) {
      for (int i = 0; i < state.length; i++) {
        state[i] *= state[15];  // at least diffuse the counter
      }
    }

    @Override
    int[] initialState(byte[] nonce, int counter) {
      IntBuffer b = ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
      return new int[]{
          b.get(), b.get(), b.get(), b.get(), 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, counter};
    }

    @Override
    void incrementCounter(int[] state) {
      state[15]++;
    }

    @Override
    int nonceSizeInBytes() {
      return 0;
    }
  }

  @Test
  public void testStateGenNoRead() {
    byte[] nonce = new byte[16];
    nonce[0] = 2;
    nonce[4] = 2;
    nonce[8] = 2;
    nonce[12] = 2;
    StateGen stateGen = new StateGen(new MockDJBCipher(new byte[32]), nonce, 3);
    assertThat(stateGen.next()).isEqualTo(
        new int[] {8, 8, 8, 8, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 12});
    assertThat(stateGen.next()).isEqualTo(
        new int[] {10, 10, 10, 10, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 20});
  }

  @Test
  public void testStateGenReadSomeFirst() {
    byte[] nonce = new byte[16];
    nonce[0] = 2;
    nonce[4] = 2;
    nonce[8] = 2;
    nonce[12] = 2;
    StateGen stateGen = new StateGen(new MockDJBCipher(new byte[32]), nonce, 3);
    assertThat(stateGen.read(5)).isEqualTo(
        new int[] {8, 8, 8, 8, 4});
    assertThat(stateGen.next()).isEqualTo(
        new int[] {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 12, 10, 10, 10, 10, 5});
    assertThat(stateGen.next()).isEqualTo(
        new int[] {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 20, 12, 12, 12, 12, 6});
  }

  @Test
  public void testStateGenReadLengthGT16ThrowsIllegalArgException() {
    StateGen stateGen = new StateGen(new MockDJBCipher(new byte[32]), new byte[16], 3);
    try {
      stateGen.read(16);
      fail("Expected IllegalArgumentException.");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().containsMatch("length must be less than 16. length: 16");
    }
  }

  @Test
  public void testStateGenReadCalledTwiceThrowsIllegalStateException() {
    StateGen stateGen = new StateGen(new MockDJBCipher(new byte[32]), new byte[16], 3);
    stateGen.read(15);
    try {
      stateGen.read(1);
      fail("Expected IllegalStateException.");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().containsMatch(
          "read can only be called once and before next().");
    }
  }

  @Test
  public void testStateGenReadCalledAfterNextThrowsIllegalStateException() {
    StateGen stateGen = new StateGen(new MockDJBCipher(new byte[32]), new byte[16], 3);
    stateGen.next();
    try {
      stateGen.read(1);
      fail("Expected IllegalStateException.");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().containsMatch(
          "read can only be called once and before next().");
    }
  }
}
