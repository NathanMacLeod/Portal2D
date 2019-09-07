/*
 * File added by Nathan MacLeod 2019
 */
package portal2d;

import java.util.ArrayList;
/**
 *
 * @author Nathan
 */
public class AssetListWrapper {
    ArrayList<Asset> assets;
    ArrayList<ActivationPair> activatables;
    
    public AssetListWrapper(ArrayList<Asset> assets, ArrayList<ActivationPair> activatables) {
        this.assets = assets;
        this.activatables = activatables;
    }
    
    public Elavator getEntryElavator() {
        for(Asset a : assets) {
            if(a instanceof Elavator && ((Elavator)a).isEntrance())
                return ((Elavator)a);
        }
        return null;
    }
    
    public Elavator getExitElavator() {
        for(Asset a : assets) {
            if(a instanceof Elavator && !((Elavator)a).isEntrance())
                return ((Elavator)a);
        }
        return null;
    }
}
