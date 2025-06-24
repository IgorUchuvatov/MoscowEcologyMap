package com.example.ecologemoscow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {
    private static final String TAG = "PlacesFragment";
    private RecyclerView parksRecyclerView;
    private ParksAdapter parksAdapter;
    private List<Park> parks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Creating view");
        View view = inflater.inflate(R.layout.fragment_places, container, false);
        
        parksRecyclerView = view.findViewById(R.id.parks_recycler_view);

        if (parksRecyclerView == null) {
            Log.e(TAG, "RecyclerView is null");
            return view;
        }
        
        Log.d(TAG, "Setting up RecyclerView");
        parksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        parks = new ArrayList<>();
        parksAdapter = new ParksAdapter(getContext(), parks);
        parksRecyclerView.setAdapter(parksAdapter);
        
        loadDemoData();
        
        return view;
    }

    private void loadDemoData() {
        Log.d(TAG, "Loading demo data");
        parks.clear();
        
        // Парк Горького
        parks.add(new Park(
            "Парк Горького",
            "/place/park-gorkogo/",
            "Центральный парк культуры и отдыха имени Горького - один из самых известных парков Москвы. Здесь можно покататься на велосипеде, поиграть в пляжный волейбол, посетить музеи и выставки.",
            55.7287,
            37.6038,
            10
        ));
        
        // Сокольники
        parks.add(new Park(
            "Сокольники",
            "/place/sokolniki/",
            "Парк Сокольники - один из старейших парков Москвы с богатой историей. Здесь есть спортивные площадки, велодорожки, роллердром и многое другое.",
            55.7897,
            37.6732,
            8
        ));
        
        // ВДНХ
        parks.add(new Park(
            "ВДНХ",
            "/place/vdnh/",
            "Выставка достижений народного хозяйства - крупнейший выставочный комплекс в России. Здесь можно увидеть уникальные павильоны, фонтаны и архитектурные памятники.",
            55.8297,
            37.6322,
            9
        ));
        
        // Коломенское
        parks.add(new Park(
            "Коломенское",
            "/place/kolomenskoe/",
            "Музей-заповедник Коломенское - бывшая царская резиденция с уникальными памятниками архитектуры. Здесь сохранились старинные церкви, дворцы и сады.",
            55.6667,
            37.6833,
            8
        ));
        
        // Царицыно
        parks.add(new Park(
            "Царицыно",
            "/place/tsaritsyno/",
            "Музей-заповедник Царицыно - дворцово-парковый ансамбль на юге Москвы. Здесь можно увидеть Большой дворец, Хлебный дом и другие архитектурные памятники.",
            55.6167,
            37.6833,
            9
        ));

        // Зарядье
        parks.add(new Park(
            "Парк «Зарядье»",
            "/place/park-zaryade/",
            "Современный парк в самом центре Москвы с уникальной архитектурой и технологиями. Здесь есть «парящий мост», медиацентр, ледяная пещера и четыре характерные для России природные зоны.",
            55.7514,
            37.6289,
            10
        ));

        // Измайловский
        parks.add(new Park(
            "Измайловский парк",
            "/place/izmajlovskij-park/",
            "Один из крупнейших парков Москвы с богатой историей. Здесь находится старинная усадьба Измайлово, множество прудов и лесных массивов.",
            55.7868,
            37.7448,
            6
        ));

        // Кузьминки
        parks.add(new Park(
            "Кузьминки",
            "/place/kuzminki/",
            "Природно-исторический парк с усадебным комплексом. Здесь есть конный двор, музыкальная эстрада, детские площадки и множество прогулочных маршрутов.",
            55.7007,
            37.7858,
            7
        ));

        // Фили
        parks.add(new Park(
            "Парк Фили",
            "/place/park-fili/",
            "Природный парк на берегу Москвы-реки. Отличное место для активного отдыха с велодорожками, спортплощадками и пляжной зоной.",
            55.7472,
            37.4856,
            8
        ));

        // Серебряный Бор
        parks.add(new Park(
            "Серебряный Бор",
            "/place/serebryanyj-bor/",
            "Уникальный природный комплекс на искусственном острове. Здесь есть песчаные пляжи, сосновый бор и места для купания.",
            55.7847,
            37.4307,
            9
        ));

        // Лосиный Остров
        parks.add(new Park(
            "Лосиный Остров",
            "/place/losinyj-ostrov/",
            "Национальный парк в черте города. Здесь можно увидеть диких животных в естественной среде обитания, есть экологические тропы и биостанция.",
            55.8691,
            37.7457,
            7
        ));

        // Воробьевы горы
        parks.add(new Park(
            "Воробьевы горы",
            "/place/vorobevy-gory/",
            "Природный заказник с потрясающей смотровой площадкой. Отсюда открывается панорамный вид на Москву, есть канатная дорога и экотропы.",
            55.7108,
            37.5555,
            8
        ));

        // Ботанический сад
        parks.add(new Park(
            "Главный ботанический сад",
            "/place/botanicheskij-sad/",
            "Крупнейший ботанический сад Европы. Здесь собрана уникальная коллекция растений со всего мира, есть японский сад и розарий.",
            55.8352,
            37.6058,
            10
        ));

        // Сад Баумана
        parks.add(new Park(
            "Сад имени Баумана",
            "/place/sad-baumana/",
            "Уютный сад в центре Москвы. Здесь есть летний кинотеатр, детские площадки и зона воркаута.",
            55.7656,
            37.6636,
            7
        ));

        // Парк Победы
        parks.add(new Park(
            "Парк Победы",
            "/place/park-pobedy/",
            "Мемориальный комплекс на Поклонной горе. Здесь находятся музей Великой Отечественной войны, храм Георгия Победоносца и множество памятников.",
            55.7352,
            37.5098,
            9
        ));
        
        Log.d(TAG, "Added " + parks.size() + " demo parks");
        parksAdapter.notifyDataSetChanged();
    }
} 