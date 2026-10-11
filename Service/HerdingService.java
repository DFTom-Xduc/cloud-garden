package Service;
import Entity.*;
import java.util.Map;
import static Service.Json.object;

/** Clock-derived routes also handle skipped ticks and reloads without replaying events. */
public final class HerdingService {
    public static final double ROUTE_MINUTES=50;
    private HerdingService() {}
    public static double pastureFraction(double minutes,boolean dog) {
        if(!dog)return 0;
        double m=minutes%1440;
        if(m<360)return 0;
        if(m<1080)return Math.min(1,(m-360)/ROUTE_MINUTES);
        return Math.max(0,1-(m-1080)/ROUTE_MINUTES);
    }
    public static Map<String,Object> status(GameState game) {
        double m=game.gameMinutes()%1440;boolean sheep=game.animals.containsKey("cuu"),goat=game.animals.containsKey("de"),dog=game.guardDog!=null;
        String mode=!sheep?"NO_SHEEP":!dog?"NEEDS_DOG":m>=360&&m<410?"LEAVING":m>=1080&&m<1130?"RETURNING":m>=410&&m<1080?"GRAZING":"INDOORS";
        String duty=!dog?"NONE":mode.equals("LEAVING")?"HERD_OUT":mode.equals("RETURNING")?"HERD_IN":goat?"GUARD_GOATS":"GUARD_BARN";
        return object("mode",mode,"duty",duty,"pastureFraction",pastureFraction(game.gameMinutes(),dog),"routeMinutes",ROUTE_MINUTES,"outHour",6,"inHour",18);
    }
}
