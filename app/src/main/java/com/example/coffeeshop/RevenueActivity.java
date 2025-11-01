package com.example.coffeeshop;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.coffeeshop.database.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RevenueActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView tvTotalRevenue, tvFilteredRevenue, tvFilterLabel;
    private RadioGroup radioGroupFilter;
    private Button btnSelectDate;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        setupToolbar();
        initViews();
        loadRevenue();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Revenue Statistics");
        }
    }

    private void initViews() {
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        tvFilteredRevenue = findViewById(R.id.tv_filtered_revenue);
        tvFilterLabel = findViewById(R.id.tv_filter_label);
        radioGroupFilter = findViewById(R.id.radio_group_filter);
        btnSelectDate = findViewById(R.id.btn_select_date);
    }

    private void loadRevenue() {
        double totalRevenue = databaseHelper.getTotalRevenue();
        tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", totalRevenue));
    }

    private void setupListeners() {
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            btnSelectDate.setVisibility(View.VISIBLE);
            tvFilteredRevenue.setVisibility(View.VISIBLE);
            tvFilterLabel.setVisibility(View.VISIBLE);
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    calculateFilteredRevenue();
                }, year, month, day);

        datePickerDialog.show();
    }

    private void calculateFilteredRevenue() {
        int selectedId = radioGroupFilter.getCheckedRadioButtonId();
        double revenue = 0;
        String filterText = "";

        if (selectedId == R.id.radio_day) {
            String date = dateFormat.format(calendar.getTime());
            revenue = databaseHelper.getRevenueByDate(date);
            filterText = "Revenue for " + date;
        } else if (selectedId == R.id.radio_month) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            revenue = databaseHelper.getRevenueByMonth(year, month);
            filterText = String.format(Locale.getDefault(), "Revenue for %04d-%02d", year, month);
        } else if (selectedId == R.id.radio_year) {
            int year = calendar.get(Calendar.YEAR);
            revenue = databaseHelper.getRevenueByYear(year);
            filterText = "Revenue for " + year;
        }

        tvFilterLabel.setText(filterText);
        tvFilteredRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}