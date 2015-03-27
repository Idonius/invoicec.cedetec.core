package com.cimait.invoicec.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.util.util.key.Environment;
import com.util.util.key.Util;

public class DigitalSignature {

	public static Document add(Document doc, String parent) throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException,
			FileNotFoundException, IOException, UnrecoverableKeyException,
			InvalidAlgorithmParameterException, KeyException, MarshalException,
			XMLSignatureException, XMLSecurityException, XPathExpressionException {
		
		String providerName = System.getProperty("jsr105Provider",
				"org.jcp.xml.dsig.internal.dom.XMLDSigRI");

		final XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance(
				"DOM", (Provider) Class.forName(providerName).newInstance());

		KeyStore keyStore = KeyStore.getInstance(Util.keystore_type);
		keyStore.load(new FileInputStream(Util.keystore_file),
				Util.keystore_password.toCharArray());

		PrivateKey privateKey = (PrivateKey) keyStore.getKey(Util.private_key_alias,
				Util.private_key_password.toCharArray());

		X509Certificate cert = (X509Certificate) keyStore
				.getCertificate(Util.private_key_alias);
		PublicKey publicKey = cert.getPublicKey();
		String referenceURI = ""; // todo el documento

		List<Transform> transforms = Collections.singletonList(sigFactory
				.newTransform(Transform.ENVELOPED,
						(TransformParameterSpec) null));

		Reference ref = sigFactory.newReference(referenceURI,
				sigFactory.newDigestMethod(DigestMethod.SHA1, null),
				transforms, null, null);
		
		SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory
				.newCanonicalizationMethod(
						CanonicalizationMethod.EXCLUSIVE_WITH_COMMENTS,
						(C14NMethodParameterSpec) null), sigFactory
				.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
				Collections.singletonList(ref));
		KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
		

		List x509Content = new ArrayList();
		x509Content.add(cert.getSubjectX500Principal().getName());
		x509Content.add(cert);
		
		X509Data x509d = keyInfoFactory.newX509Data(x509Content);
		
		KeyValue keyValue = keyInfoFactory.newKeyValue(publicKey);

		List<X509Data> keyInfoItems = new ArrayList<X509Data>();
		// keyInfoItems.add(keyValue);
		keyInfoItems.add(x509d);

		KeyInfo keyInfo = keyInfoFactory.newKeyInfo(keyInfoItems);

		//Element sigParent = doc.getDocumentElement();
		XPath xpath = XPathFactory.newInstance().newXPath();
		Node sigParent = (Node) xpath.compile("(//*/ExtensionContent)["+parent+"]").evaluate( doc, XPathConstants.NODE);
		DOMSignContext dsc = new DOMSignContext(privateKey, sigParent);
		dsc.setDefaultNamespacePrefix("ds");
		
		XMLSignature signature = sigFactory
				.newXMLSignature(signedInfo, keyInfo,null,"SignatureCTLSA",null);

		signature.sign(dsc);

		return doc;
	}

}
