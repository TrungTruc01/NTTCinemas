package com.example.nttcinemas.Admin

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.firestore.FirebaseFirestore
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class StatisticsFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var pieChart: PieChart
    private lateinit var paymentListView: ListView
    private lateinit var spinnerTimeSelection: Spinner
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnExportExcel: Button
    private lateinit var btnExportWord: Button

    private val ticketList = mutableListOf<Ticket>()
    private val filteredTickets = mutableListOf<Ticket>()

    private var selectedTimeOption: String = "Tuần"
    private var selectedFilterOption: String = "Tất cả"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        pieChart = view.findViewById(R.id.pieChart)
        paymentListView = view.findViewById(R.id.paymentListView)
        spinnerTimeSelection = view.findViewById(R.id.spinnerTimeSelection)
        spinnerFilter = view.findViewById(R.id.spinnerFilter)
        btnExportExcel = view.findViewById(R.id.btnExportExcel)
        btnExportWord = view.findViewById(R.id.btnExportWord)

        setupTimeSelectionSpinner()
        loadTicketsFromFirestore()

        btnExportExcel.setOnClickListener { exportDataToExcel() }
        btnExportWord.setOnClickListener { exportDataToWord() }
    }

    private fun setupTimeSelectionSpinner() {
        val timeOptions = arrayOf("Tuần", "Tháng", "Năm")
        spinnerTimeSelection.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, timeOptions)

        spinnerTimeSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTimeOption = timeOptions[position]
                updateFilterOptions()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateFilterOptions() {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = 2024

        val filterOptions = when (selectedTimeOption) {
            "Tuần" -> (1..52).map { "Tuần $it" }.toTypedArray()
            "Tháng" -> (1..12).map { "Tháng $it" }.toTypedArray()
            "Năm" -> (2024..currentYear).map { "Năm $it" }.toTypedArray()
            else -> emptyArray()
        }

        val defaultSelection = when (selectedTimeOption) {
            "Tuần" -> "Tuần $currentWeek"
            "Tháng" -> "Tháng $currentMonth"
            "Năm" -> "Năm $currentYear"
            else -> null
        }

        spinnerFilter.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filterOptions)

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFilterOption = filterOptions[position]
                filterTickets()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        defaultSelection?.let {
            val defaultPosition = filterOptions.indexOf(it)
            if (defaultPosition != -1) {
                spinnerFilter.setSelection(defaultPosition)
            }
        }
    }

    private fun loadTicketsFromFirestore() {
        firestore.collection("tickets").get().addOnSuccessListener { querySnapshot ->
            ticketList.clear()
            for (document in querySnapshot) {
                val ticket = document.toObject(Ticket::class.java)
                ticketList.add(ticket)
            }
            filterTickets()
        }.addOnFailureListener {
            Toast.makeText(context, "Lỗi tải dữ liệu thống kê", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterTickets() {
        filteredTickets.clear()
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        filteredTickets.addAll(ticketList.filter { ticket ->
            try {
                val ticketDate = sdf.parse(ticket.bookingDate) ?: return@filter false
                calendar.time = ticketDate

                val matchesTimeFilter = when (selectedTimeOption) {
                    "Tuần" -> {
                        val selectedWeek = selectedFilterOption.replace("Tuần ", "").toInt()
                        calendar.get(Calendar.WEEK_OF_YEAR) == selectedWeek
                    }
                    "Tháng" -> {
                        val selectedMonth = selectedFilterOption.replace("Tháng ", "").toInt()
                        calendar.get(Calendar.MONTH) + 1 == selectedMonth
                    }
                    "Năm" -> {
                        val selectedYear = selectedFilterOption.replace("Năm ", "").toInt()
                        calendar.get(Calendar.YEAR) == selectedYear
                    }
                    else -> true
                }

                matchesTimeFilter
            } catch (e: ParseException) {
                false
            }
        })

        updateListView()
        updatePieChart()
    }

    private fun updateListView() {
        val listItems = filteredTickets.map {
            "Phim: ${it.movieTitle}\nNgày: ${it.bookingDate}\nSố tiền: ${it.price}\nHình thức: ${it.paymentMethod ?: "Chưa rõ"}"
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, listItems)
        paymentListView.adapter = adapter
    }

    private fun updatePieChart() {
        val entries = ArrayList<PieEntry>()

        filteredTickets.groupBy { it.paymentMethod ?: "Khác" }.forEach { (key, value) ->
            val totalAmount = value.sumOf { ticket -> ticket.price }
            entries.add(PieEntry(totalAmount.toFloat(), key))
        }

        if (entries.isEmpty()) {
            Toast.makeText(context, "Không có dữ liệu để hiển thị biểu đồ", Toast.LENGTH_SHORT).show()
            return
        }

        val dataSet = PieDataSet(entries, "Thống kê theo loại thanh toán")
        dataSet.colors = listOf(
            resources.getColor(R.color.purple_200, null),
            resources.getColor(R.color.teal_200, null),
            resources.getColor(R.color.blue, null),
            resources.getColor(R.color.red, null)
        )

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.invalidate()
    }

    private fun getExportDirectory(): File {
        val exportDir = File(requireContext().getExternalFilesDir(null), "Cinemas NTT/File xuất")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        return exportDir
    }

    private fun exportDataToExcel() {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Statistics")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Tên phim")
        headerRow.createCell(1).setCellValue("Số tiền")
        headerRow.createCell(2).setCellValue("Loại thanh toán")
        headerRow.createCell(3).setCellValue("Ngày thanh toán")

        filteredTickets.forEachIndexed { index, ticket ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(ticket.movieTitle)
            row.createCell(1).setCellValue(ticket.price.toString())
            row.createCell(2).setCellValue(ticket.paymentMethod ?: "Chưa rõ")
            row.createCell(3).setCellValue(ticket.bookingDate)
        }

        val exportDir = getExportDirectory()
        val file = File(exportDir, "Thống kê thanh toán.xlsx")

        try {
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            Toast.makeText(context, "Đã xuất file Excel tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi xuất file Excel: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun exportDataToWord() {
        val document = XWPFDocument()
        val title = document.createParagraph()
        title.alignment = ParagraphAlignment.CENTER
        val titleRun = title.createRun()
        titleRun.isBold = true
        titleRun.fontSize = 16
        titleRun.setText("Thống kê thanh toán")

        filteredTickets.forEach { ticket ->
            val paragraph = document.createParagraph()
            val run = paragraph.createRun()
            run.setText(
                "Phim: ${ticket.movieTitle}\n" +
                        "Số tiền: ${ticket.price}\n" +
                        "Loại thanh toán: ${ticket.paymentMethod ?: "Chưa rõ"}\n" +
                        "Ngày thanh toán: ${ticket.bookingDate}\n"
            )
        }

        val exportDir = getExportDirectory()
        val file = File(exportDir, "Thống kê thanh toán.docx")

        try {
            FileOutputStream(file).use { document.write(it) }
            document.close()
            Toast.makeText(context, "Đã xuất file Word tại: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khi xuất file Word: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
