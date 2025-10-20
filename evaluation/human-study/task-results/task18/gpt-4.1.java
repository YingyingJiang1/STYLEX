
private boolean verifyCertificateLicense(String licenseFile, LicenseContext context) {
    try {
        String encodedPayload = licenseFile;
        encodedPayload = encodedPayload.replace(CERT_PREFIX, "");
        encodedPayload = encodedPayload.replace(CERT_SUFFIX, "");
        encodedPayload = encodedPayload.replaceAll("\\r?\\n", "");

        byte[] payloadBytes = Base64.getDecoder().decode(encodedPayload);
        String payload = new String(payloadBytes);

        log.info("Decoded certificate payload: {}", payload);

        String encryptedData = "";
        String encodedSignature = "";
        String algorithm = "";
        try {
            JSONObject attrs = new JSONObject(payload);
            encryptedData = (String) attrs.get("enc");
            encodedSignature = (String) attrs.get("sig");
            algorithm = (String) attrs.get("alg");
        } catch (JSONException e) {
            log.error("Failed to parse license file: {}", e.getMessage());
            return false;
        }

        if (!"base64+ed25519".equals(algorithm)) {
            log.error("Unsupported algorithm: {}. Only base64+ed25519 is supported.", algorithm);
            return false;
        }

        boolean isSignatureValid = verifyEd25519Signature(encryptedData, encodedSignature);
        if (!isSignatureValid) {
            log.error("License file signature is invalid");
            return false;
        }
        log.info("License file signature is valid");

        String decodedData;
        try {
            decodedData = new String(Base64.getDecoder().decode(encryptedData));
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode license data: {}", e.getMessage());
            return false;
        }

        boolean isValid = processCertificateData(decodedData, context);
        return isValid;
    } catch (Exception e) {
        log.error("Error verifying certificate license: {}", e.getMessage(), e);
        return false;
    }
}
