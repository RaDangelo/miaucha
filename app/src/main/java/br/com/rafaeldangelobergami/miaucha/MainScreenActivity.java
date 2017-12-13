package br.com.rafaeldangelobergami.miaucha;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Button btnProfile = (Button) findViewById(R.id.profileButton);
        Button btnFindCats = (Button) findViewById(R.id.findCatsButton);
        Button registerCatButton = (Button) findViewById(R.id.registerCatButton);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreenActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
        btnFindCats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreenActivity.this, CatSearchActivity.class);
                startActivity(intent);
            }
        });
        registerCatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainScreenActivity.this, CatRegistrationActivity.class);
                startActivity(intent);
            }
        });
    }
}
