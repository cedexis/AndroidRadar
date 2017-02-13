// Copyright 2016 Cedexis
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge,
// publish, distribute, sublicense, and/or sell copies of the Software,
// and to permit persons to whom the Software is furnished to do so.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.

package com.cedexis.androidradar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Allows the user to configure various properties of the Radar session.
 */
public class RadarSessionProperties implements Parcelable {
    private int _requestorZoneId;
    private int _requestorCustomerId;
    private RadarImpactProperties _impactProperties;
    private double _throughputSampleRate;
    private double _throughputSampleRateMobile;

    /**
     * This constructor allows the developer to specify most aspects of the Radar session.
     *
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     * @param impactProperties This property is reserved for future use and should be set to `null`.
     * @param throughputSampleRate The percentage at which to downsample throughput measurements when not on a mobile network (e.g. on WiFi).  Specify a decimal number from 0 to 1.
     * @param throughputSampleRateMobile The percentage at which to downsample throughput measurements on mobile networks.  Specify a decimal number from 0 to 1.
     */
    public RadarSessionProperties(
            int requestorZoneId,
            int requestorCustomerId,
            RadarImpactProperties impactProperties,
            double throughputSampleRate,
            double throughputSampleRateMobile) {
        this._requestorZoneId = requestorZoneId;
        this._requestorCustomerId = requestorCustomerId;
        this._impactProperties = impactProperties;
        this._throughputSampleRate = throughputSampleRate;
        this._throughputSampleRateMobile = throughputSampleRateMobile;
    }

    /**
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     * @param impactProperties This property is reserved for future use and should be set to `null`.
     */
    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId, RadarImpactProperties impactProperties) {
        this(requestorZoneId, requestorCustomerId, impactProperties, 1, 0);
    }

    /**
     * Use this constructor for the most basic scenario with default settings.
     *
     * @param requestorZoneId The Cedexis Zone ID of the customer. Usually 1.
     * @param requestorCustomerId The Cedexis Customer ID.
     */
    public RadarSessionProperties(int requestorZoneId, int requestorCustomerId) {
        this(requestorZoneId, requestorCustomerId, null);
    }

    protected RadarSessionProperties(Parcel in) {
        _requestorZoneId = in.readInt();
        _requestorCustomerId = in.readInt();
        _throughputSampleRate = in.readDouble();
        _throughputSampleRateMobile = in.readDouble();
        _impactProperties = (RadarImpactProperties)in.readSerializable();
    }

    public static final Creator<RadarSessionProperties> CREATOR = new Creator<RadarSessionProperties>() {
        @Override
        public RadarSessionProperties createFromParcel(Parcel in) {
            return new RadarSessionProperties(in);
        }

        @Override
        public RadarSessionProperties[] newArray(int size) {
            return new RadarSessionProperties[size];
        }
    };

    /**
     * @return TODO
     */
    public int getRequestorZoneId() {
        return _requestorZoneId;
    }

    /**
     * @return TODO
     */
    public int getRequestorCustomerId() {
        return _requestorCustomerId;
    }

    /**
     * @return TODO
     */
    public double getThroughputSampleRate() {
        return _throughputSampleRate;
    }

    /**
     * @return TODO
     */
    public double getThroughputSampleRateMobile() {
        return _throughputSampleRateMobile;
    }

    /**
     * @return TODO
     */
    public RadarImpactProperties getImpactProperties() {
        return _impactProperties;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_requestorZoneId);
        dest.writeInt(_requestorCustomerId);
        dest.writeSerializable(_impactProperties);
        dest.writeDouble(_throughputSampleRate);
        dest.writeDouble(_throughputSampleRateMobile);
    }
}
