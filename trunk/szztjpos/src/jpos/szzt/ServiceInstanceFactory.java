/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jpos.szzt;

import java.lang.reflect.Constructor;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

/**
 *
 * @author Maxim
 */
public class ServiceInstanceFactory implements JposServiceInstanceFactory {

    /**
     * Simply creates an instance of a service.
     * @param logicalName the logical name for this entry
     * @param entry the JposEntry with properties for creating the service
     * @since 0.1 (Philly 99 meeting)
     * @exception jpos.JposException in case the factory cannot create service or service throws exception
     */
    public JposServiceInstance createInstance( String logicalName, JposEntry entry ) throws JposException
    {
        if( !entry.hasPropertyWithName( JposEntry.SERVICE_CLASS_PROP_NAME ) )
            throw new JposException( JposConst.JPOS_E_NOSERVICE, "The JposEntry does not contain the 'serviceClass' property" );

        JposServiceInstance serviceInstance = null;

        try
        {
            String serviceClassName = (String)entry.getPropertyValue( JposEntry.SERVICE_CLASS_PROP_NAME );
            Class serviceClass = Class.forName( serviceClassName );

            Constructor ctor = serviceClass.getConstructor();

            serviceInstance = (JposServiceInstance)ctor.newInstance();

            DeviceService ds = (DeviceService)serviceInstance;

            ds.setJposEntry( entry );
        }
        catch( Exception e )
        { throw new JposException( JposConst.JPOS_E_NOSERVICE, "Could not create the service instance!", e ); }

        return serviceInstance;
    }

}
