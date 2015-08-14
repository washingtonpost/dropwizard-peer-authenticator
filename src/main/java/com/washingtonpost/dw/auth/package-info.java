package com.washingtonpost.arc.auth.service.peer;

/**
 * <p>This package provides a stand-alone implementation of a DropWizard Authenticator interface that our principle REST service
 * can use to validate any requests coming to its /auth endpoint are themselves valid.</p>
 *
 * <p>That is to say, we don't just allow any old request to show up and try to get UserContextData responses with some random
 * key.  The requestor must itself be on a known list of authorized callers of this service.  Because we don't anticipate
 * many peers to this application, the initial implementation of the AllowedPeerAuthenticator is done with a FlatFilePeerDAO
 * that just reads in allowed users & passwords from a plain text Properties file.</p>
 *
 * <p>The plan for the future is to swap out the implementation of the PeerDAO with something that connects to a more durable
 * and maintainable source</p>
 *
 */