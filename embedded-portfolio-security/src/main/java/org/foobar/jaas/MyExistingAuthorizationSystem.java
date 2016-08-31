package org.foobar.jaas;

public class MyExistingAuthorizationSystem {
	String[] generateRolesForAUser(String username) {
		// let's say only "testUser" has "select" role
		if ("testUser".equals(username)) {
			return new String[] {"select"};
		}

		return new String[] {};
	}

	static MyExistingAuthorizationSystem instance() {
		return new MyExistingAuthorizationSystem();
	}
}
