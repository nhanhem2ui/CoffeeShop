package com.example.coffeeshop;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.coffeeshop.database.DatabaseHelper;
import com.example.coffeeshop.utils.LocaleHelper;
import com.example.coffeeshop.utils.SessionManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RevenueActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView tvTotalRevenue, tvFilteredRevenue, tvFilterLabel;
    private RadioGroup radioGroupFilter;
    private Button btnSelectDate;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleHelper.applyLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        if (!SessionManager.getInstance().isAdmin()) {
            Toast.makeText(this, R.string.access_denied, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProductListActivity.class));
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        calendar = Calendar.getInstance();

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
            getSupportActionBar().setTitle(R.string.revenue_statistics);
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
            tvFilteredRevenue.setVisibility(View.GONE);
            tvFilterLabel.setVisibility(View.GONE);

            // Update button text based on selection
            if (checkedId == R.id.radio_day) {
                btnSelectDate.setText(R.string.select_day);
            } else if (checkedId == R.id.radio_month) {
                btnSelectDate.setText(R.string.select_month);
            } else if (checkedId == R.id.radio_year) {
                btnSelectDate.setText(R.string.select_year);
            }
        });

        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int selectedId = radioGroupFilter.getCheckedRadioButtonId();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        if (selectedId == R.id.radio_day) {
            // Show full date picker for day selection
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(selectedYear, selectedMonth, selectedDay);
                        calculateDayRevenue();
                    }, year, month, day);
            datePickerDialog.show();

        } else if (selectedId == R.id.radio_month) {
            // Show month/year picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calculateMonthRevenue();
                    }, year, month, day);

            // Hide day picker
            try {
                datePickerDialog.getDatePicker().findViewById(
                        getResources().getIdentifier("day", "id", "android")
                ).setVisibility(View.GONE);
            } catch (Exception e) {
                // Fallback: just show the full picker
            }

            datePickerDialog.show();

        } else if (selectedId == R.id.radio_year) {
            // Show year picker only
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calculateYearRevenue();
                    }, year, month, day);

            // Hide month and day picker
            try {
                datePickerDialog.getDatePicker().findViewById(
                        getResources().getIdentifier("month", "id", "android")
                ).setVisibility(View.GONE);
                datePickerDialog.getDatePicker().findViewById(
                        getResources().getIdentifier("day", "id", "android")
                ).setVisibility(View.GONE);
            } catch (Exception e) {
                // Fallback: just show the full picker
            }

            datePickerDialog.show();
        }
    }

    private void calculateDayRevenue() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());
        double revenue = databaseHelper.getRevenueByDate(date);

        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String filterText = getString(R.string.revenue_for_date, displayFormat.format(calendar.getTime()));

        tvFilterLabel.setText(filterText);
        tvFilteredRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
        tvFilterLabel.setVisibility(View.VISIBLE);
        tvFilteredRevenue.setVisibility(View.VISIBLE);
    }

    private void calculateMonthRevenue() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        double revenue = databaseHelper.getRevenueByMonth(year, month);

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String filterText = getString(R.string.revenue_for_date, monthFormat.format(calendar.getTime()));

        tvFilterLabel.setText(filterText);
        tvFilteredRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
        tvFilterLabel.setVisibility(View.VISIBLE);
        tvFilteredRevenue.setVisibility(View.VISIBLE);
    }

    private void calculateYearRevenue() {
        int year = calendar.get(Calendar.YEAR);
        double revenue = databaseHelper.getRevenueByYear(year);

        String filterText = getString(R.string.revenue_for_year, year);

        tvFilterLabel.setText(filterText);
        tvFilteredRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
        tvFilterLabel.setVisibility(View.VISIBLE);
        tvFilteredRevenue.setVisibility(View.VISIBLE);
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