package org.teiid.example;

import static org.teiid.example.JDBCUtils.execute;
import static org.teiid.example.JDBCUtils.executeQuery;

import org.h2.constant.SysProperties;
import org.h2.tools.RunScript;
import org.teiid.jdbc.TeiidSQLException;
import org.teiid.resource.adapter.file.FileManagedConnectionFactory;
import org.teiid.runtime.EmbeddedConfiguration;
import org.teiid.runtime.EmbeddedServer;
import org.teiid.translator.file.FileExecutionFactory;
import org.teiid.translator.jdbc.h2.H2ExecutionFactory;

import javax.sql.DataSource;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Properties;
import java.util.logging.Level;

public class TeiidEmbeddedPortfolioSecurityCustom {

public static void main(String[] args) throws Exception {
        
        EmbeddedHelper.enableLogger(Level.INFO);
        
        DataSource ds = EmbeddedHelper.newDataSource("org.h2.Driver", "jdbc:h2:mem://localhost/~/account", "sa", "sa");
        RunScript.execute(ds.getConnection(), new InputStreamReader(TeiidEmbeddedPortfolioSecurityCustom.class.getClassLoader().getResourceAsStream("data/customer-schema.sql")));
        
        EmbeddedServer server = new EmbeddedServer();
        
        H2ExecutionFactory executionFactory = new H2ExecutionFactory() ;
        executionFactory.setSupportsDirectQueryProcedure(true);
        executionFactory.start();
        server.addTranslator("translator-h2", executionFactory);
        
        server.addConnectionFactory("java:/accounts-ds", ds);
        
        FileExecutionFactory fileExecutionFactory = new FileExecutionFactory();
        fileExecutionFactory.start();
        server.addTranslator("file", fileExecutionFactory);
        
        FileManagedConnectionFactory managedconnectionFactory = new FileManagedConnectionFactory();
        managedconnectionFactory.setParentDirectory("src/main/resources/data");
        server.addConnectionFactory("java:/marketdata-file", managedconnectionFactory.createConnectionFactory());
    
        EmbeddedConfiguration config = new EmbeddedConfiguration();
        config.setTransactionManager(EmbeddedHelper.getTransactionManager());
        config.setSecurityDomain("teiid-security-custom");
        config.setSecurityHelper(EmbeddedHelper.getSecurityHelper());
        server.start(config);
                
        
        server.deployVDB(TeiidEmbeddedPortfolioSecurityCustom.class.getClassLoader().getResourceAsStream("portfolio-vdb.xml"));

		System.out.println("select as testUser should pass:");

        Properties info = new Properties();
        info.put("user", "testUser");
        info.put("password", "custom login module does't read password");
        Connection c = server.getDriver().connect("jdbc:teiid:Portfolio;version=1", info);

		executeQuery(c, "select * from Stock");

		System.out.println("select as kylin should fail (not a user):");

		info.put("user", "kylin");

		c = server.getDriver().connect("jdbc:teiid:Portfolio;version=1", info);

		try {
			executeQuery(c, "select * from Stock");
		}
		catch (TeiidSQLException x) {
			System.out.println("error : " + x.getMessage());
		}
    }

}
