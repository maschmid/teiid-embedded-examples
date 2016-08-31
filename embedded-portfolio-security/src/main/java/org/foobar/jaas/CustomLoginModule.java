package org.foobar.jaas;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomLoginModule {
	private Subject subject;
	private String[] roles;
	private String username;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	private Map options;

	public CustomLoginModule() {
		System.out.println("Login Module - constructor called");
	}

	public boolean abort() throws LoginException {
		System.out.println("Login Module - abort called");
		return false;
	}

	public boolean commit() throws LoginException {
		System.out.println("Login Module - commit called");

		// assign roles to the subject

		subject.getPrincipals().add(() -> username);

		Set<Principal> principalRoles = new HashSet<>();
		for (String role : roles) {
			principalRoles.add(() -> role);

			System.out.println("adding role: " + role);
		}

		subject.getPrincipals().add(new Group() {
			@Override
			public boolean addMember(Principal principal) {
				return principalRoles.add(principal);
			}

			@Override
			public boolean removeMember(Principal principal) {
				return principalRoles.remove(principal);
			}

			@Override
			public boolean isMember(Principal principal) {
				return principalRoles.stream().anyMatch(p -> p.getName().equals(principal.getName()));
			}

			@Override
			public Enumeration<? extends Principal> members() {
				return Collections.enumeration(principalRoles);
			}

			@Override
			public String getName() {
				return "Roles";
			}
		});

		return true;
	}

	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options) {

		System.out.println("Login Module - initialize called");
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		// System.out.println("testOption value: " + (String) options.get("testOption"));
	}

	public boolean login() throws LoginException {
		System.out.println("Login Module - login called");
		if (callbackHandler == null) {
			throw new LoginException("Oops, callbackHandler is null");
		}

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("name:");
		callbacks[1] = new PasswordCallback("password:", false);

		try {
			callbackHandler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException("Oops, IOException calling handle on callbackHandler");
		} catch (UnsupportedCallbackException e) {
			throw new LoginException("Oops, UnsupportedCallbackException calling handle on callbackHandler");
		}

		NameCallback nameCallback = (NameCallback) callbacks[0];
		PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

		String name = nameCallback.getName();
		String password = new String(passwordCallback.getPassword());

		// We ignore password, as we are rely on an existing custom authentication system

		// We generate principals based on our custom authorization system
		roles = MyExistingAuthorizationSystem.instance().generateRolesForAUser(name).clone();
		username = name;

		return true;
	}

	public boolean logout() throws LoginException {
		System.out.println("Login Module - logout called");
		return false;
	}
}
