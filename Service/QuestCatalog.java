package Service;
import Entity.Quest;
import java.util.List;

public final class QuestCatalog {
    private QuestCatalog() {}
    public static final List<Quest> ALL=List.of(
        new Quest("first_harvest","Mùa gặt đầu tiên","First harvest","Thu hoạch 6 ô hoa. Đào bỏ không được tính.","Harvest 6 flower plots. Digging does not count.","harvest",6,120,1,""),
        new Quest("sow","Người gieo mầm","Seed keeper","Trồng thêm 8 cây hoa.","Plant 8 flowers.","plant",8,70,0,""),
        new Quest("water","Giọt nước xanh","Fresh water","Tưới hoa thành công 12 lần.","Water flowers 12 times.","water",12,80,0,""),
        new Quest("bloom","Bông hoa mạnh mẽ","Strong bloom","Nâng 3 cây hoa lên cấp 3.","Grow 3 flowers to level 3.","level3",3,100,1,""),
        new Quest("feed_ga","Bữa sáng cho gà","Chicken breakfast","Cho gà ăn thóc 3 lần.","Feed chickens grain 3 times.","feed_ga",3,55,0,"ga"),
        new Quest("eggs","Giỏ trứng đầu tiên","Egg basket","Thu 6 quả trứng vào kho.","Collect 6 eggs.","collect_EGG",6,65,0,"ga"),
        new Quest("feed_lon","Chăm sóc lợn","Pig keeper","Cho lợn ăn 2 lượt nuôi.","Feed pigs in 2 cycles.","feed_lon",2,90,0,"lon"),
        new Quest("pork","Sản phẩm từ khu bùn","Mud pen produce","Thu 4 phần thịt lợn.","Collect 4 portions of pork.","collect_PORK",4,85,0,"lon"),
        new Quest("feed_bo","Đồng cỏ dinh dưỡng","Cattle pasture","Mua cỏ và cho bò ăn 2 lượt.","Buy grass and feed cattle in 2 cycles.","feed_bo",2,110,0,"bo"),
        new Quest("beef","Thành quả chăn nuôi","Cattle produce","Thu 5 phần thịt bò.","Collect 5 portions of beef.","collect_BEEF",5,100,0,"bo"),
        new Quest("market","Phiên chợ nông trại","Farm market","Bán sản phẩm chăn nuôi tổng cộng 150 coin.","Sell livestock products worth 150 coins.","sold",150,80,0,""),
        new Quest("expand","Một vùng đất mới","New horizons","Mở rộng khu vườn.","Expand the garden.","expanded",1,60,1,""),
        new Quest("habitats","Nông trại đầy đủ","Complete farm","Có đủ chuồng gà, khu bùn và chuồng bò.","Own a coop, mud pen and cattle barn.","habitats",3,100,1,""),
        new Quest("cottage","Mái ấm mới","New cottage","Xây nhà gỗ trên khu đất mở rộng.","Build a cottage on expanded land.","cottage",1,75,0,""),
        new Quest("hill_farm","Đồng cỏ mới","New pastures","Xây chuồng cừu và trang trại dê.","Build both sheep and goat farms.","hill_habitats",2,120,1,""),
        new Quest("shepherd","Người bạn canh gác","A watchful friend","Mua chó lùa cừu và canh dê.","Buy a shepherd dog.","guard_dog",1,50,0,"cuu"),
        new Quest("feed_cuu","Bữa ăn đồng cỏ","Sheep supper","Cho cừu ăn 3 lượt.","Feed sheep in 3 cycles.","feed_cuu",3,85,0,"cuu"),
        new Quest("wool","Giỏ lông mềm","Soft fleece","Xén 8 phần lông cừu.","Collect 8 portions of wool.","collect_WOOL",8,100,0,"cuu"),
        new Quest("feed_de","Đàn dê khỏe mạnh","Healthy goats","Cho dê ăn 2 lượt.","Feed goats in 2 cycles.","feed_de",2,90,0,"de"),
        new Quest("goat_meat","Sản phẩm trang trại dê","Goat farm produce","Thu 5 phần thịt dê.","Collect 5 portions of goat meat.","collect_GOAT_MEAT",5,100,0,"de")
    );
    public static Quest find(String id) {return ALL.stream().filter(q->q.id().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Unknown quest"));}
}
