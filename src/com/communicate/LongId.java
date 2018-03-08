package com.communicate;

import java.util.Date;

/*
 * Generates a unique ID based on milliseconds and an optional server Id.
 * Builds the following as a hex string and converts to a long:
 *    (11 hex digits timestamp) (2 hex digits counter) (3 hex digits server ID)
 * Server IDs can range from 0 - 4095
 * A counter resolves collisions where two or more IDs are created within the same millisecond.
 * We can extract the timestamp or server ID from any ID generated using this scheme.
 *
 * Usage:
 *
 *   LongId li = new LongId(1) - creates an instance to generate IDs with the embedded server ID 1.
 *   li.getNewId() - generates IDs with the embedded server Id on the instance.
 *
 * Once you have a generated id, you can retrieve information from it, with or without an instance.
 *
 *   LongId.getDate(id) - extract the timestamp from the ID.
 *   LongId.getServerId(id) - extract the server ID from the ID.
 *
 * MIT License
 *
 * Copyright (c) 2018 Communicate.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class LongId {

    // How many hex digits we use for each component of the ID
    private static final int MILLIS_HEX_DIGITS = 11;
    private static final int COUNTER_HEX_DIGITS = 2;
    private static final int SERVER_HEX_DIGITS = 3;

    private static final int COUNTER_MAX = 255;
    private static final int SERVER_MAX = 4095;
    // For padding
    private static final String ZEROES = "00000000000";

    // State variables, used to ensure unique IDs when called within the same millisecond.
    // Shared across instances, so even if you create two objects with the same server, the IDs will still be unique
    private static long millisPrevious = 0;
    private static long counterWithinThisMilli = 0;


    // Optional server ID will be 0. Can be set by creating a new LongId(serverId);
    private final long serverId;

    // Create a new instance and set the server Id to 0.
    public LongId() {
        serverId = 0;
    }
    // Create a new instance and set the server Id.
    public LongId(long setServerId) {
        if (setServerId > SERVER_MAX || setServerId < 0) {
            throw new IllegalArgumentException("Server Id must be in the range 0-" + SERVER_MAX);
        }
        serverId = setServerId;
    }

    // Generate a new ID with the given instance's server ID
    public long getNewId() {
        return getNewIdStatic(serverId);
    }

    // Generate a new ID. Synchronized so that each thread will wait for the previous one to finish, allowing us
    // to maintain state and guarantee a unique ID when two threads hit within the same millisecond.
    private static synchronized long getNewIdStatic(long serverId) {
        // store the current millis since epoch
        long millisCurrent = System.currentTimeMillis();

        // if NOT within the same milli, reset static vars  (safe since Synchronized)
        if (millisPrevious != millisCurrent) {
            millisPrevious = millisCurrent;
            counterWithinThisMilli = 0;
        }
        // if counter is maxed out, sleep 1ms, then call self recursively
        else if (counterWithinThisMilli >= COUNTER_MAX) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // sleep throws a checked exception, so we have to deal with it here or else kick it upstairs and force the caller to deal with it.
                throw new RuntimeException(e);
            }
            return getNewIdStatic(serverId); // recursive call
        }
        // if within the same milli, increment counter
        else {
            counterWithinThisMilli++;
        }

        // store counter
        millisPrevious = millisCurrent;

        // convert millisCurrent to hex. No need to pad it since it's going to be at the beginning.
        String millisAsHex = Long.toHexString(millisCurrent);

        // convert counter to hex, padded with zeroes as needed.
        String counterAsHex = Long.toHexString(counterWithinThisMilli);
        counterAsHex = ZEROES.substring(0,COUNTER_HEX_DIGITS-counterAsHex.length()) + counterAsHex;

        // convert serverId to hex, padded with zeroes as needed.
        String serverAsHex = Long.toHexString(serverId);
        serverAsHex = ZEROES.substring(0,SERVER_HEX_DIGITS-serverAsHex.length()) + serverAsHex;

        // concatenate them together and decode to Long
        return Long.decode("0x" + millisAsHex + counterAsHex + serverAsHex);
    }

    // Get the date that a LongId was generated.
    // Drop the last 6 hex digits and the rest will be the timestamp
    public static Date getDate(long microId) {
        String hexInput = Long.toHexString(microId);
        if (hexInput.length() < COUNTER_HEX_DIGITS+SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return new Date(Long.decode("0x" + hexInput.substring(0, hexInput.length()-(COUNTER_HEX_DIGITS+SERVER_HEX_DIGITS) )));
    }

    // Get the server on which a LongId was generated.
    // Convert number to hex. Take the last few hex digits. Convert them back to a number. That's the server Id.
    public static long getServerId(long microId) {
        String hexInput = Long.toHexString(microId);
        if (hexInput.length() < COUNTER_HEX_DIGITS+SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return Long.decode("0x" + hexInput.substring(hexInput.length()-SERVER_HEX_DIGITS) );
    }

    // Get the counter from a LongId. Not significant except for debugging.
    public static long getCounter(long microId) {
        String hexInput = Long.toHexString(microId);
        if (hexInput.length() < COUNTER_HEX_DIGITS+SERVER_HEX_DIGITS + 1)
            throw new IllegalArgumentException("Input is too short to be a LongId");
        return Long.decode("0x" + hexInput.substring(hexInput.length()-(COUNTER_HEX_DIGITS+SERVER_HEX_DIGITS),hexInput.length()-SERVER_HEX_DIGITS) );
    }
}
