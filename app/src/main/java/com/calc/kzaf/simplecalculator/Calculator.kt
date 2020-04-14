package com.calc.kzaf.simplecalculator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_calculator.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("IMPLICIT_CAST_TO_ANY")
class Calculator : AppCompatActivity() {

    private var firstNum = ""
    private var secondNum = ""
    private var symbolVar = ""

    private var calculated = false

    private var noConnectionFlag = false

    private var symbolsResponse: JSONObject? = null
    private var namesResponse: JSONObject? = null

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
                initializeCalculatorScreen()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_currency -> {
                if (!noConnectionFlag){
                    initializeCurrencyScreen()
                    return@OnNavigationItemSelectedListener true
                }else{
                    Toast.makeText(this, "No internet connection! Check connections and try again!", Toast.LENGTH_SHORT).show()
                    getRequest() // Load the currency values onCreate
                }
            }
        }
        false
    }

    private fun initializeCalculatorScreen(){
        calculator_linear_laout.visibility = View.VISIBLE
        currency_linear_layout.visibility = View.INVISIBLE
        clearScreen()
        expression.text = ""
        val actionBar = supportActionBar
        clearCurrencyScreen()
        actionBar?.title = "Calculator"
    }

    private fun initializeCurrencyScreen(){
        getRequest()

        currency_linear_layout.visibility = View.VISIBLE
        calculator_linear_laout.visibility = View.INVISIBLE
        val actionBar = supportActionBar
        actionBar?.title = "Currency Converter"
    }

    private fun validateCalculatorButtons() {
        zero.setOnClickListener { updateExpressionNumber("0") }
        one.setOnClickListener { updateExpressionNumber("1") }
        two.setOnClickListener { updateExpressionNumber("2") }
        three.setOnClickListener { updateExpressionNumber("3") }
        four.setOnClickListener { updateExpressionNumber("4") }
        five.setOnClickListener { updateExpressionNumber("5") }
        six.setOnClickListener { updateExpressionNumber("6") }
        seven.setOnClickListener { updateExpressionNumber("7") }
        eight.setOnClickListener { updateExpressionNumber("8") }
        nine.setOnClickListener { updateExpressionNumber("9") }
        comma.setOnClickListener { updateExpressionNumber("."); comma.isClickable = false}

        add_button.setOnClickListener { updateExpressionSymbol("+") }
        sub_button.setOnClickListener { updateExpressionSymbol("-") }
        multiply_button.setOnClickListener { updateExpressionSymbol("*") }
        div_button.setOnClickListener { updateExpressionSymbol("/") }

        result.setOnClickListener { calculate() }

        clear.setOnClickListener { clearScreen(); expression.text = "" }
    }

    private fun validateCurrencyButtons() {
        convert_button.setOnClickListener { currencyConvert() }
        swap.setOnClickListener{ swapCurrencies() }
        clear_button.setOnClickListener{ clearCurrencyScreen() }
    }

    // Calculator Methods
    private fun updateExpressionNumber(number: String){
        when {
            expression.length() < 15 || calculated -> {
                val alreadyExistingExpression = expression.text
                expression.text = "$alreadyExistingExpression$number"
                if (firstNum == "") firstNum = number
                if (symbolVar != "") secondNum += number
                if(calculated){ alreadyCalculatedNumber(number) }
            }
            else -> Toast.makeText(this, "Too long expression", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateExpressionSymbol(symbol: String){
        when {
            expression.length() < 15 || calculated -> {
                val alreadyExistingExpression = expression.text
                firstNum = alreadyExistingExpression.toString()
                when {
                    firstNum != "" && symbolVar == "" -> {
                        expression.text = "$firstNum $symbol "
                        symbolVar = symbol
                        comma.isClickable = true
                    }
                }
                if(calculated){ alreadyCalculatedSymbol(symbol) }
            }
            else -> Toast.makeText(this, "Too long expression", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculate() {
        if (firstNum != "" && secondNum != "" && symbolVar != "") {

            var valueOne = firstNum.toDouble()
            val valueTwo = secondNum.toDouble()
            operator.text = "="

            when {
                symbolVar === "+" -> valueOne += valueTwo
                symbolVar === "-" -> valueOne -= valueTwo
                symbolVar === "*" -> valueOne *= valueTwo
                symbolVar === "/" -> valueOne /= valueTwo
            }

            // Checks if result is a whole number or not
            when {
                valueOne - Math.floor(valueOne) == 0.0 -> {
                    val formatted = String.format("%.0f", valueOne)
                    calc_result.text = formatted
                }
                else -> calc_result.text = valueOne.toString()
            }
            calculated = true
            comma.isClickable = false

        } else {
            try {
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun alreadyCalculatedNumber(number: String) {
        comma.isClickable = true
        firstNum = number
        symbolVar = ""
        secondNum = ""
        expression.text = number
        operator.text = ""
        calc_result.text = ""
        calculated = false
    }

    private fun alreadyCalculatedSymbol(symbol: String) {
        val result = calc_result.text
        firstNum = result.toString()
        symbolVar = symbol
        secondNum = ""
        expression.text = "$result $symbol "
        operator.text = ""
        calc_result.text = ""
        calculated = false
    }

    private fun clearScreen() {
        calc_result.text = ""
        operator.text = ""
        firstNum = ""
        secondNum = ""
        symbolVar = ""
        comma.isClickable = true
        calculated = false
    }



    // Currency Methods
    @SuppressLint("SetTextI18n")
    private fun currencyConvert() = when {
        !amount_value.text.isNullOrEmpty() -> {

            val namesAndValuesMap = matchCurrencyNamesWithCodes(symbolsResponse, namesResponse)

            currency_result.text =
                    calculateEquivalent(namesAndValuesMap[from_spinner.selectedItem.toString()]!!,
                            namesAndValuesMap[to_spinner.selectedItem.toString()]!!,
                            amount_value.text!!).take(9)
            currency_symbol.text = to_spinner.selectedItem.toString()

            equivalent_from.text = "1 " + from_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(namesAndValuesMap[from_spinner.selectedItem.toString()]!!,
                    namesAndValuesMap[to_spinner.selectedItem.toString()]!!, 1).take(4) + " " + to_spinner.selectedItem.toString()
            equivalent_to.text = "1 " + to_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(namesAndValuesMap[to_spinner.selectedItem.toString()]!!,
                    namesAndValuesMap[from_spinner.selectedItem.toString()]!!, 1).take(4) + " "+from_spinner.selectedItem.toString()
            equivalent_to.paintFlags = equivalent_to.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            equivalent_from.paintFlags = equivalent_from.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
        }
        else -> Toast.makeText(this, "Please set an amount", Toast.LENGTH_SHORT).show()
    }

    private fun clearCurrencyScreen(){
        currency_result.text = null
        amount_value.text = null
        equivalent_from.text = null
        equivalent_to.text = null
        currency_symbol.text = null

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
        val urlCurrencyRates = "http://data.fixer.io/api/latest?access_key=2f75648f00880488578eabf872c617c5"
        val urlCurrencySymbols = "http://data.fixer.io/api/symbols?access_key=2f75648f00880488578eabf872c617c5"

        MySingleton.getInstance(this).addToRequestQueue(JsonObjectRequest(Request.Method.GET, urlCurrencyRates, null,
                Response.Listener { response ->
                    completionHandlerForRates(response)
                    symbolsResponse = response.getJSONObject("rates")
                    noConnectionFlag = false
                },
                Response.ErrorListener {
                    noConnectionFlag = true
                }
        ))

        MySingleton.getInstance(this).addToRequestQueue(JsonObjectRequest(Request.Method.GET, urlCurrencySymbols, null,
                Response.Listener { response ->
                    completionHandlerForSymbols(response)
                    namesResponse = response.getJSONObject("symbols")
                    noConnectionFlag = false
                },
                Response.ErrorListener {
                    noConnectionFlag = true
                }
        ))
    }

    private var symbolsCollection = HashMap<String, Any>()
    private fun completionHandlerForSymbols(response: JSONObject?){
        symbolsCollection = collectAllRates(response?.getJSONObject("symbols")!!)
    }

    private var ratesCollection = HashMap<String, Any>()
    private fun completionHandlerForRates(response: JSONObject?) {
        ratesCollection = collectAllRates(response?.getJSONObject("rates")!!)

        val currencyNames = ArrayList(ratesCollection.keys)
        currencyNames.sort()

        val currencySymbols = ArrayList(symbolsCollection.values)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencySymbols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.adapter = adapter
        to_spinner.adapter = adapter
        from_spinner.setTitle("From")
        to_spinner.setTitle("To")
    }

    private val currencyCodesAndSymbols: HashMap<String, Any> = hashMapOf()
    private fun matchCurrencyNamesWithCodes(rates: JSONObject?, names: JSONObject?): HashMap<String, Any>{

        for (i in 0 until rates!!.names().length()) {
            val keyRates = rates.names().getString(i)
            val valueRates = rates.get(keyRates)
            val keyNames = names!!.names().getString(i)
            val valueNames = names.get(keyNames).toString()
            if(keyRates == keyNames){ currencyCodesAndSymbols[valueNames] = valueRates }
        }
        return currencyCodesAndSymbols
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
            (amount.toString().toDouble() * currency2.toString().toDouble() / currency1.toString().toDouble()).toString()
    }