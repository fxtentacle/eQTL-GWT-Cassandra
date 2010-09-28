/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
package org.apache.cassandra.service;

/*
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;

/**
 * RPC timeout was exceeded. either a node failed mid-operation, or load was too
 * high, or the requested op was too large.
 */
public class TimedOutException extends Exception implements TBase, java.io.Serializable, Cloneable, Comparable<TimedOutException> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final TStruct STRUCT_DESC = new TStruct("TimedOutException");

	public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {
		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;

		{
		}
	});

	static {
		FieldMetaData.addStructMetaDataMap(TimedOutException.class, metaDataMap);
	}

	public TimedOutException() {
	}

	/**
	 * Performs a deep copy on <i>other</i>.
	 */
	public TimedOutException(TimedOutException other) {
	}

	public TimedOutException deepCopy() {
		return new TimedOutException(this);
	}

	@Override
	@Deprecated
	public TimedOutException clone() {
		return new TimedOutException(this);
	}

	public void setFieldValue(int fieldID, Object value) {
		switch (fieldID) {
		default:
			throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
		}
	}

	public Object getFieldValue(int fieldID) {
		switch (fieldID) {
		default:
			throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
		}
	}

	// Returns true if field corresponding to fieldID is set (has been asigned a
	// value) and false otherwise
	public boolean isSet(int fieldID) {
		switch (fieldID) {
		default:
			throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
		}
	}

	@Override
	public boolean equals(Object that) {
		if (that == null)
			return false;
		if (that instanceof TimedOutException)
			return this.equals((TimedOutException) that);
		return false;
	}

	public boolean equals(TimedOutException that) {
		if (that == null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	public int compareTo(TimedOutException other) {
		if (!getClass().equals(other.getClass())) {
			return getClass().getName().compareTo(other.getClass().getName());
		}

		return 0;
	}

	public void read(TProtocol iprot) throws TException {
		TField field;
		iprot.readStructBegin();
		while (true) {
			field = iprot.readFieldBegin();
			if (field.type == TType.STOP) {
				break;
			}
			switch (field.id) {
			default:
				TProtocolUtil.skip(iprot, field.type);
				break;
			}
			iprot.readFieldEnd();
		}
		iprot.readStructEnd();

		// check for required fields of primitive type, which can't be checked
		// in the validate method
		validate();
	}

	public void write(TProtocol oprot) throws TException {
		validate();

		oprot.writeStructBegin(STRUCT_DESC);
		oprot.writeFieldStop();
		oprot.writeStructEnd();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TimedOutException(");
		sb.append(")");
		return sb.toString();
	}

	public void validate() throws TException {
		// check for required fields
		// check that fields of type enum have valid values
	}

}
