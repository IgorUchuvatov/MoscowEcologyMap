package com.example.ecologemoscow;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShopsFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private DatabaseReference shopsRef;
    private List<Shop> shops;
    private View shopInfoView;
    private boolean isInfoVisible = false;
    private BitmapDescriptor customMarkerIcon;
    private FloatingActionButton homeButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.shops_fragment, container, false);
        
        // Инициализация Firebase
        shopsRef = FirebaseDatabase.getInstance().getReference("shops");
        shops = new ArrayList<>();

        // Инициализация кнопки возврата
        homeButton = view.findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> returnToDistrictsMap());

        // Инициализация кастомной иконки маркера
        customMarkerIcon = getBitmapDescriptor(R.drawable.iconshop);

        // Инициализация карты
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.shops_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Инициализация view для информации о магазине
        shopInfoView = view.findViewById(R.id.shop_info_container);
        shopInfoView.setVisibility(View.GONE);

        // Кнопка закрытия информации о магазине
        view.findViewById(R.id.close_shop_info).setOnClickListener(v -> {
            shopInfoView.setVisibility(View.GONE);
            isInfoVisible = false;
        });

        return view;
    }

    private void returnToDistrictsMap() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private BitmapDescriptor getBitmapDescriptor(int vectorResId) {
        if (getContext() == null) return null;
        
        Drawable vectorDrawable = ContextCompat.getDrawable(getContext(), vectorResId);
        if (vectorDrawable == null) return null;

        int size = 120;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // drawable
        vectorDrawable.setBounds(0, 0, size, size);

        vectorDrawable.draw(canvas);
        
        // круглый битмап
        Bitmap outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas outputCanvas = new Canvas(outputBitmap);
        
        // Настраиваем Paint для создания круглой маски
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        outputCanvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        
        // Настраиваем Paint для наложения изображения
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        outputCanvas.drawBitmap(bitmap, 0, 0, paint);
        
        return BitmapDescriptorFactory.fromBitmap(outputBitmap);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        
        LatLng moscow = new LatLng(55.751999, 37.617499);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 10));

        
        addTestShops();

        
        googleMap.setOnMarkerClickListener(marker -> {
            Shop shop = (Shop) marker.getTag();
            if (shop != null) {
                showShopInfo(shop);
                return true;
            }
            return false;
        });
    }

    private void addTestShops() {
        // Тестовые магазины с подробными описаниями
        Shop[] testShops = {
            new Shop(
                "ЭкоМаркет Южное Бутово",
                "09:00", "21:00", 55.5425, 37.5308,
                "ЭкоМаркет Южное Бутово — это современный магазин экологически чистых продуктов и товаров для дома. Здесь вы найдете широкий ассортимент органических овощей и фруктов, натуральных молочных продуктов, а также бытовую химию без вредных добавок.\n\nМагазин уделяет особое внимание качеству и происхождению товаров, сотрудничая только с проверенными поставщиками. Для постоянных клиентов действуют скидки и бонусные программы."
            ),
            new Shop(
                "Зеленый Мир",
                "10:00", "22:00", 55.5725, 37.5608,
                "Зеленый Мир — это уютный магазин, специализирующийся на продаже эко-товаров для всей семьи. В ассортименте представлены продукты для вегетарианцев, веганов и людей, следящих за своим здоровьем.\n\nЗдесь можно приобрести натуральную косметику, эко-игрушки для детей и товары для раздельного сбора мусора. В магазине регулярно проходят мастер-классы и лекции по экологичному образу жизни."
            ),
            new Shop(
                "ЭкоПром",
                "08:00", "20:00", 55.5575, 37.5458,
                "ЭкоПром — это сеть магазинов, предлагающих только сертифицированные органические продукты. Особое внимание уделяется местным производителям и сезонным товарам.\n\nВ магазине можно получить консультацию по вопросам здорового питания и подобрать индивидуальный рацион. Для покупателей действует система лояльности и бесплатная доставка по району."
            ),
            new Shop(
                "Природа",
                "09:00", "21:00", 55.5625, 37.5358,
                "Природа — магазин, где собраны лучшие товары для здорового образа жизни. Здесь вы найдете свежие фермерские продукты, натуральные напитки, а также товары для йоги и фитнеса.\n\nМагазин поддерживает местные эко-инициативы и проводит акции по сбору вторсырья. Для новых клиентов предусмотрены приветственные скидки."
            ),
            new Shop(
                "ЭкоПлюс",
                "10:00", "22:00", 55.5475, 37.5508,
                "ЭкоПлюс — это магазин, ориентированный на современные эко-решения для дома и офиса. В ассортименте — биоразлагаемая упаковка, многоразовые бутылки, эко-гаджеты и многое другое.\n\nМагазин сотрудничает с ведущими российскими и зарубежными брендами, а также организует обучающие мероприятия для всех желающих."
            )
        };

        // Добавляем маркеры для тестовых магазинов
        for (Shop shop : testShops) {
            addShopMarker(shop);
        }
    }

    private void addShopMarker(Shop shop) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(shop.getLatitude(), shop.getLongitude()))
                .icon(customMarkerIcon)
                .title(shop.getName());

        Marker marker = googleMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setTag(shop);
        }
    }

    private void showShopInfo(Shop shop) {
        // Вместо показа фрагмента — запуск отдельной активности
        android.content.Intent intent = new android.content.Intent(getContext(), ShopDetailsActivity.class);
        intent.putExtra("name", shop.getName());
        intent.putExtra("openTime", shop.getOpenTime());
        intent.putExtra("closeTime", shop.getCloseTime());
        intent.putExtra("description", shop.getDescription());
        startActivity(intent);
    }

    // Класс для хранения информации о магазине
    public static class Shop {
        private String name;
        private String openTime;
        private String closeTime;
        private double latitude;
        private double longitude;
        private String description;

        public Shop() {}

        public Shop(String name, String openTime, String closeTime, double latitude, double longitude, String description) {
            this.name = name;
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.latitude = latitude;
            this.longitude = longitude;
            this.description = description;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getOpenTime() { return openTime; }
        public void setOpenTime(String openTime) { this.openTime = openTime; }
        public String getCloseTime() { return closeTime; }
        public void setCloseTime(String closeTime) { this.closeTime = closeTime; }
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
