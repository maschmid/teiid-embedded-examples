package org.foobar.jaas;

public class MyExistingAuthorizationSystem {
	String[] generateRolesForAUser(String username) {
		// let's say only "testUser is "user"
		if ("testUser".equals(username)) {
			return new String[] {"user"};
		}

		return new String[] {};
	}

	static MyExistingAuthorizationSystem instance() {
		return new MyExistingAuthorizationSystem();
	}
}
