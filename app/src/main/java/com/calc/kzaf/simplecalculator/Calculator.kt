package com.calc.kzaf.simplecalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_calculator.*
import org.json.JSONObject
import java.util.*

class Calculator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val actionBar = supportActionBar
        actionBar?.title = "Calculator"

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        getRequest() // Load the currency values onCreate

        validateCalculatorButtons()
        validateCurrencyButtons()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_calculator -> {
                calculator_linear_laout.visibility = View.VISIBLE
                currency_table.visibility = View.INVISIBLE
                clearScreen()
                first_number.text = "0"
                val actionBar = supportActionBar
                clearCurrencyScreen()
                actionBar?.title = "Calculator"

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_currency -> {
                currency_table.visibility = View.VISIBLE
                calculator_linear_laout.visibility = View.INVISIBLE
                val actionBar = supportActionBar
                actionBar?.title = "Currency Converter"


                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun validateCalculatorButtons() {
        zero.setOnClickListener { updateNumbers(0) }
        one.setOnClickListener { updateNumbers(1) }
        two.setOnClickListener { updateNumbers(2) }
        three.setOnClickListener { updateNumbers(3) }
        four.setOnClickListener { updateNumbers(4) }
        five.setOnClickListener { updateNumbers(5) }
        six.setOnClickListener { updateNumbers(6) }
        seven.setOnClickListener { updateNumbers(7) }
        eight.setOnClickListener { updateNumbers(8) }
        nine.setOnClickListener { updateNumbers(9) }

        add_button.setOnClickListener { updateSymbols("+") }
        sub_button.setOnClickListener { updateSymbols("-") }
        multiply_button.setOnClickListener { updateSymbols("*") }
        div_button.setOnClickListener { updateSymbols("/") }

        comma.setOnClickListener { commaUpdate() }

        result.setOnClickListener { calculate() }

        clear.setOnClickListener {
            clearScreen()
            first_number.text = "0"
        }
    }

    private fun validateCurrencyButtons() {
        convert_button.setOnClickListener { currencyConvert() }
        swap.setOnClickListener{ swapCurrencies() }
        clear_button.setOnClickListener{ clearCurrencyScreen() }
    }

    // Calculator Methods
    private fun clearScreen() {
        second_num.text = "0"
        second_num.visibility = View.INVISIBLE
        operator.visibility = View.INVISIBLE
    }

    private fun updateSymbols(symbol: String) {
        operator.text = symbol
        operator.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun commaUpdate() {
        when {
            operator.visibility == View.VISIBLE -> when {
                !second_num.text.contains(".") -> {
                    second_num.visibility = View.VISIBLE
                    second_num.text = second_num.text.toString() + "."
                }
            }
            !first_number.text.contains(".") -> first_number.text = first_number.text.toString() + "."
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateNumbers(number: Int) {
        when {
            operator.visibility == View.VISIBLE -> {
                second_num.visibility = View.VISIBLE
                when {
                    second_num.text.toString() == "0" -> second_num.text = number.toString()
                    else -> second_num.text = second_num.text.toString() + number.toString()
                }
            }
            else -> when {
                first_number.text.toString() == "0" -> first_number.text = number.toString()
                else -> first_number.text = first_number.text.toString() + number.toString()
            }
        }
    }

    private fun calculate() {
        if (!(first_number.text.toString().toDouble().isNaN() || second_num.text.toString().toDouble().isNaN())) {

            var valueOne = first_number.text.toString().toDouble()
            val valueTwo = second_num.text.toString().toDouble()

            when {
                operator.text.toString() === "+" -> valueOne += valueTwo
                operator.text.toString() === "-" -> valueOne -= valueTwo
                operator.text.toString() === "*" -> valueOne *= valueTwo
                operator.text.toString() === "/" -> valueOne /= valueTwo
            }

            first_number.text = valueOne.toString()
            clearScreen()

        } else {
            try {
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    // Currency Methods
    @SuppressLint("SetTextI18n")
    private fun currencyConvert() = when {
        !amount_value.text.isNullOrEmpty() -> {
            currency_result.text =
                    calculateEquivalent(ratesCollection[from_spinner.selectedItem.toString()]!!,
                            ratesCollection[to_spinner.selectedItem.toString()]!!,
                            amount_value.text!!).take(9)
            currency_symbol.text = to_spinner.selectedItem.toString()

            equivalent_from.text = "1 " + from_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(ratesCollection[from_spinner.selectedItem.toString()]!!,
                    ratesCollection[to_spinner.selectedItem.toString()]!!, 1).take(4) + " " + to_spinner.selectedItem.toString()
            equivalent_to.text = "1 " + to_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(ratesCollection[to_spinner.selectedItem.toString()]!!,
                    ratesCollection[from_spinner.selectedItem.toString()]!!, 1).take(4) + " "+from_spinner.selectedItem.toString()

        }
        else -> Toast.makeText(this, "Please set an amount", Toast.LENGTH_SHORT).show()
    }

    private fun clearCurrencyScreen(){
        currency_result.text = null
        amount_value.text = null
        equivalent_from.text = null
        equivalent_to.text = null

        from_spinner.setSelection(0)
        to_spinner.setSelection(0)
    }

    private fun swapCurrencies(){
        val fromSpinnerIndex = from_spinner.selectedItemPosition

        from_spinner.setSelection(to_spinner.selectedItemPosition)
        to_spinner.setSelection(fromSpinnerIndex)

        currencyConvert()
    }

    private fun getRequest() {
        val url = "http://data.fixer.io/api/latest?access_key=2f75648f00880488578eabf872c617c5"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    completionHandler(response)
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error?.message, Toast.LENGTH_SHORT).show()
                }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private var ratesCollection = HashMap<String, Any>()

    private fun completionHandler(response: JSONObject?) {
        ratesCollection = collectAllRates(response?.getJSONObject("rates")!!)

        val currencyNames = ArrayList(ratesCollection.keys)
        currencyNames.sort()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.adapter = adapter
        to_spinner.adapter = adapter
    }

    private fun collectAllRates(rates: JSONObject): HashMap<String, Any>{
        val ratesHashMap: HashMap<String, Any> = hashMapOf()

        (0 until rates.names().length()).forEach { i ->
            val key = rates.names().getString(i)
            val value = rates.get(key)

            ratesHashMap[key] = value
        }
        return ratesHashMap
    }

    private fun calculateEquivalent(currency1: Any, currency2: Any, amount: Any): String =
            (amount.toString().toDouble() * currency1.toString().toDouble() / currency2.toString().toDouble()).toString()
    }