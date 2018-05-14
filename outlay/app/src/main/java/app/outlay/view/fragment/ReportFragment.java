package app.outlay.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import app.outlay.Constants;
import app.outlay.core.utils.DateUtils;
import app.outlay.domain.model.Report;
import app.outlay.impl.AndroidLogger;
import app.outlay.mvp.presenter.ReportPresenter;
import app.outlay.mvp.view.StatisticView;
import app.outlay.view.Navigator;
import app.outlay.view.adapter.ReportAdapter;
import app.outlay.view.dialog.DatePickerFragment;
import app.outlay.view.fragment.base.BaseMvpFragment;
import app.outlay.view.helper.OnTabSelectedListenerAdapter;
import app.outlay.view.model.CategorizedExpenses;
import butterknife.Bind;

/**
 * Created by Bogdan Melnychuk on 1/20/16.
 */
public class ReportFragment extends BaseMvpFragment<StatisticView, ReportPresenter> implements StatisticView {
    public static final String ARG_DATE = "_argDate";


    @Bind(app.outlay.R.id.recyclerView)
    RecyclerView recyclerView;

    @Bind(app.outlay.R.id.tabs)
    TabLayout tabLayout;

    @Bind(app.outlay.R.id.toolbar)
    Toolbar toolbar;

    @Bind(app.outlay.R.id.noResults)
    View noResults;

    @Inject
    ReportPresenter reportPresenter;

    private int selectedPeriod;
    private Date selectedDate;
    private ReportAdapter adapter;
    private AndroidLogger androidLogger = new AndroidLogger();

    @Override
    public ReportPresenter createPresenter() {
        return reportPresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApp().getUserComponent().inject(this);
        selectedDate = new Date(getArguments().getLong(ARG_DATE, new Date().getTime()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(app.outlay.R.layout.fragment_report, null, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(app.outlay.R.menu.menu_report, menu);
        MenuItem dateItem = menu.findItem(app.outlay.R.id.action_date);
        dateItem.setIcon(getResourceHelper().getMaterialToolbarIcon(app.outlay.R.string.ic_material_today));

        MenuItem listItem = menu.findItem(app.outlay.R.id.action_list);
        listItem.setIcon(getResourceHelper().getMaterialToolbarIcon(app.outlay.R.string.ic_material_list));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case app.outlay.R.id.action_date:
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.setOnDateSetListener((view, year, monthOfYear, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, monthOfYear, dayOfMonth);
                    Date selected = c.getTime();
                    analytics().trackExpensesViewDateChange(selectedDate, selected);
                    selectedDate = selected;
                    ReportFragment.this.setTitle(DateUtils.toShortString(selected));
                    updateTitle();
                    reportPresenter.getExpenses(selectedDate, selectedPeriod);
                });
                datePickerFragment.show(getChildFragmentManager(), "datePicker");
                break;
            case app.outlay.R.id.action_list:
                analytics().trackViewExpensesList();
                goToExpensesList(selectedDate, selectedPeriod);
                break;
            default:
                androidLogger.warn(Constants.DEFAULT_BRANCH_REACHED);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setToolbar(toolbar);
        setDisplayHomeAsUpEnabled(true);
        updateTitle();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        tabLayout.addTab(tabLayout.newTab().setText(app.outlay.R.string.label_day));
        tabLayout.addTab(tabLayout.newTab().setText(app.outlay.R.string.label_week));
        tabLayout.addTab(tabLayout.newTab().setText(app.outlay.R.string.label_month));
        tabLayout.getTabAt(selectedPeriod).select();
        tabLayout.addOnTabSelectedListener(new OnTabSelectedListenerAdapter() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedPeriod = tab.getPosition();
                switch (selectedPeriod) {
                    case DateUtils.PERIOD_DAY:
                        analytics().trackViewDailyExpenses();
                        break;
                    case DateUtils.PERIOD_WEEK:
                        analytics().trackViewWeeklyExpenses();
                        break;
                    case DateUtils.PERIOD_MONTH:
                        analytics().trackViewMonthlyExpenses();
                        break;
                    default:
                        androidLogger.warn(Constants.DEFAULT_BRANCH_REACHED);
                }
                updateTitle();
                reportPresenter.getExpenses(selectedDate, selectedPeriod);
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        adapter = new ReportAdapter();
        recyclerView.setAdapter(adapter);
        reportPresenter.getExpenses(selectedDate, selectedPeriod);

        adapter.setOnItemClickListener((category, report) -> goToExpensesList(selectedDate, selectedPeriod, category.getId()));
    }

    @Override
    public void showReport(Report report) {
        if (report.isEmpty()) {
            noResults.setVisibility(View.VISIBLE);
        } else {
            noResults.setVisibility(View.GONE);
            adapter.setItems(new CategorizedExpenses(report));
        }
    }

    private void updateTitle() {
        switch (selectedPeriod) {
            case DateUtils.PERIOD_DAY:
                setTitle(DateUtils.toShortString(selectedDate));
                break;
            case DateUtils.PERIOD_WEEK:
                Date startDate = DateUtils.getWeekStart(selectedDate);
                Date endDate = DateUtils.getWeekEnd(selectedDate);
                setTitle(DateUtils.toShortString(startDate) + " - " + DateUtils.toShortString(endDate));
                break;
            case DateUtils.PERIOD_MONTH:
                startDate = DateUtils.getMonthStart(selectedDate);
                endDate = DateUtils.getMonthEnd(selectedDate);
                setTitle(DateUtils.toShortString(startDate) + " - " + DateUtils.toShortString(endDate));
                break;
            default:
                androidLogger.warn(Constants.DEFAULT_BRANCH_REACHED);
        }
    }

    public void goToExpensesList(Date date, int selectedPeriod) {
        this.goToExpensesList(date, selectedPeriod, null);
    }

    public void goToExpensesList(Date date, int selectedPeriod, String category) {
        date = DateUtils.fillCurrentTime(date);
        Map<String, Date> dates = DateUtils.getDates(date, selectedPeriod);
        Date startDate = dates.get("start");
        Date endDate = dates.get("end");
        Navigator.goToExpensesList(getActivity(), startDate, endDate, category);
    }
}