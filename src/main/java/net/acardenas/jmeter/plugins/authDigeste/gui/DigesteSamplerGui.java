package net.acardenas.jmeter.plugins.authDigestE.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import net.acardenas.jmeter.plugins.authDigestE.DigesteSampler;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * User: acardenas
 */
public class DigesteSamplerGui extends AbstractSamplerGui
{

    private static final long serialVersionUID = -6044677237396079910L;
    private JLabeledTextField username;
    private JLabeledTextField password;
    private JLabeledTextField server;
    private JLabeledTextField path;

    public DigesteSamplerGui()
    {
        init();
    }

    @Override
    public String getLabelResource()
    {
        return "DigestE_Sampler";
    }

    public String getStaticLabel() 
    {
        return "DigestE Sampler";
    }

    @Override
    public TestElement createTestElement()
    {
        DigesteSampler sampler = new DigesteSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    @Override
    public void modifyTestElement(TestElement testElement)
    {
        super.configureTestElement(testElement);
        if (testElement instanceof DigesteSampler) 
        {
            DigesteSampler sampler = (DigesteSampler) testElement;
            sampler.setUserKey(username.getText());
            sampler.setUserSecret(password.getText());
            sampler.setDomain(server.getText());
            sampler.setPath(path.getText());
        }
    }

    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.add(getResourceConfigPanel(), BorderLayout.CENTER);
        add(panel, BorderLayout.CENTER);
    }

    private JPanel getResourceConfigPanel() 
    {
        username = new JLabeledTextField("username", 40);
        password = new JLabeledTextField("password",40);
        server = new JLabeledTextField("Server Name or IP", 40);
        path = new JLabeledTextField("Path", 40);

        VerticalPanel resourceConfigPanel = new VerticalPanel();
        VerticalPanel oDigestEPanel = new VerticalPanel();
        oDigestEPanel.add(username);
        oDigestEPanel.add(password);
        oDigestEPanel.add(server);
        oDigestEPanel.add(path);
        resourceConfigPanel.add(oDigestEPanel);
        return resourceConfigPanel;
    }
    
    public void configure(TestElement el) 
    {
        super.configure(el);
        DigesteSampler sampler = (DigesteSampler) el;
        username.setText(sampler.getUserKey());
        password.setText(sampler.getUserSecret());
        server.setText(sampler.getDomain());
        path.setText(sampler.getPath());
    }
    
    /**
     * Implements JMeterGUIComponent.clearGui
     */
    public void clearGui() 
    {
        super.clearGui();
        clear();
    }
    
    public void clear() 
    {
        this.username.setText("");
        this.password.setText("");
        this.server.setText("");
        this.path.setText("");
    }

}
