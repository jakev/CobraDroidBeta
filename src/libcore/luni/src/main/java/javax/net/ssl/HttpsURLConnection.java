/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.net.ssl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/* Start CobraDroid Modifications */
import android.jakev.ModState;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
/* End CobraDroid Modifications */

/**
 * This abstract subclass of {@code HttpURLConnection} defines methods for
 * managing HTTPS connections according to the description given by RFC 2818.
 */
public abstract class HttpsURLConnection extends HttpURLConnection {

    private static HostnameVerifier defaultHostnameVerifier = new DefaultHostnameVerifier();

    private static SSLSocketFactory defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory
            .getDefault();

    /**
     * Sets the default hostname verifier to be used by new instances.
     *
     * @param v
     *            the new default hostname verifier
     * @throws IllegalArgumentException
     *             if the specified verifier is {@code null}.
     */
    public static void setDefaultHostnameVerifier(HostnameVerifier v) {
        /* Start CobraDroid Modifications */ 
        // If the modification is enabled, just return.
	if (ModState.getInstance(ModState.SSL).getState()) {
                return;
        }
        
        if (v == null) {
            throw new IllegalArgumentException("HostnameVerifier is null");
        }
        defaultHostnameVerifier = v;
	/* End CobraDroid Modifications */
    }

    /**
     * Returns the default hostname verifier.
     *
     * @return the default hostname verifier.
     */
    public static HostnameVerifier getDefaultHostnameVerifier() {
        return defaultHostnameVerifier;
    }

    /**
     * Sets the default SSL socket factory to be used by new instances.
     *
     * @param sf
     *            the new default SSL socket factory.
     * @throws IllegalArgumentException
     *             if the specified socket factory is {@code null}.
     */
    public static void setDefaultSSLSocketFactory(SSLSocketFactory sf) {
        /* Start CobraDroid Modifications */
	// If the modificationn is enabled, just return.
	if (ModState.getInstance(ModState.SSL).getState()) {
                return;
        }
        
	if (sf == null) {
            throw new IllegalArgumentException("SSLSocketFactory is null");
        }
        defaultSSLSocketFactory = sf;
        /* End CobraDroid Modifications */
    }

    /**
     * Returns the default SSL socket factory for new instances.
     *
     * @return the default SSL socket factory for new instances.
     */
    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        return defaultSSLSocketFactory;
    }

    /**
     * The host name verifier used by this connection. It is initialized from
     * the default hostname verifier
     * {@link #setDefaultHostnameVerifier(HostnameVerifier)} or
     * {@link #getDefaultHostnameVerifier()}.
     */
    protected HostnameVerifier hostnameVerifier;

    private SSLSocketFactory sslSocketFactory;

    /**
     * Creates a new {@code HttpsURLConnection} with the specified {@code URL}.
     *
     * @param url
     *            the {@code URL} to connect to.
     */
    protected HttpsURLConnection(URL url) {
        super(url);
	
        /* Start CobraDroid Modifications */
	// If this modification is not enabled, just do the normal stuff
        if (!ModState.getInstance(ModState.SSL).getState()) {
		hostnameVerifier = defaultHostnameVerifier;
		sslSocketFactory = defaultSSLSocketFactory;
                return;
        }
	// However, if the modification is enabled, we need to set a HV and socket factory.
	hostnameVerifier = new HostnameVerifier()
        {
        	public boolean verify(String string, SSLSession sslSession) {
        	    return true;
        	}
        };
        
	TrustManager[] trustManager = new TrustManager[] {new X509TrustManager()
	{
	        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }

	        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }

	        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {   }
	}};

	SSLContext sslContext = null;
	    
	try {
		sslContext = SSLContext.getInstance("SSL");
	        sslContext.init(null, trustManager, new java.security.SecureRandom());
	    
	} catch (NoSuchAlgorithmException e) { }
	  catch (KeyManagementException e) { }

        sslSocketFactory = sslContext.getSocketFactory();
	/* End CobraDroid Modifications */
    }

    /**
     * Returns the name of the cipher suite negotiated during the SSL handshake.
     *
     * @return the name of the cipher suite negotiated during the SSL handshake.
     * @throws IllegalStateException
     *             if no connection has been established yet.
     */
    public abstract String getCipherSuite();

    /**
     * Returns the list of local certificates used during the handshake. These
     * certificates were sent to the peer.
     *
     * @return Returns the list of certificates used during the handshake with
     *         the local identity certificate followed by CAs, or {@code null}
     *         if no certificates were used during the handshake.
     * @throws IllegalStateException
     *             if no connection has been established yet.
     */
    public abstract Certificate[] getLocalCertificates();

    /**
     * Return the list of certificates identifying the peer during the
     * handshake.
     *
     * @return the list of certificates identifying the peer with the peer's
     *         identity certificate followed by CAs.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified..
     * @throws IllegalStateException
     *             if no connection has been established yet.
     */
    public abstract Certificate[] getServerCertificates() throws SSLPeerUnverifiedException;

    /**
     * Returns the {@code Principal} identifying the peer.
     *
     * @return the {@code Principal} identifying the peer.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified.
     * @throws IllegalStateException
     *             if no connection has been established yet.
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        Certificate[] certs = getServerCertificates();
        if (certs == null || certs.length == 0 || (!(certs[0] instanceof X509Certificate))) {
            throw new SSLPeerUnverifiedException("No server's end-entity certificate");
        }
        return ((X509Certificate) certs[0]).getSubjectX500Principal();
    }

    /**
     * Returns the {@code Principal} used to identify the local host during the handshake.
     *
     * @return the {@code Principal} used to identify the local host during the handshake, or
     *         {@code null} if none was used.
     * @throws IllegalStateException
     *             if no connection has been established yet.
     */
    public Principal getLocalPrincipal() {
        Certificate[] certs = getLocalCertificates();
        if (certs == null || certs.length == 0 || (!(certs[0] instanceof X509Certificate))) {
            return null;
        }
        return ((X509Certificate) certs[0]).getSubjectX500Principal();
    }

    /**
     * Sets the hostname verifier for this instance.
     *
     * @param v
     *            the hostname verifier for this instance.
     * @throws IllegalArgumentException
     *             if the specified verifier is {@code null}.
     */
    public void setHostnameVerifier(HostnameVerifier v) {
 	/* Start CobraDroid Modifications */
	// Do not set hostname verifier if the modification is enabled
        if (ModState.getInstance(ModState.SSL).getState()) {
                return;
        }

	if (v == null) {
            throw new IllegalArgumentException("HostnameVerifier is null");
        }
        hostnameVerifier = v;
	/* End CobraDroid Modifications */
    }

    /**
     * Returns the hostname verifier used by this instance.
     *
     * @return the hostname verifier used by this instance.
     */
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * Sets the SSL socket factory for this instance.
     *
     * @param sf
     *            the SSL socket factory to be used by this instance.
     * @throws IllegalArgumentException
     *             if the specified socket factory is {@code null}.
     */
    public void setSSLSocketFactory(SSLSocketFactory sf) {
        /* Start CobraDroid Modifications */
	// Do not set the SSL socket factory if the modification is enabled
	if (ModState.getInstance(ModState.SSL).getState()) {
                return;
        }
	if (sf == null) {
            throw new IllegalArgumentException("SSLSocketFactory is null");
        }
        sslSocketFactory = sf;
	/* End CobraDroid Modifications */
    }

    /**
     * Returns the SSL socket factory used by this instance.
     *
     * @return the SSL socket factory used by this instance.
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return sslSocketFactory;
    }

}
