package com.github.premnirmal.ticker.repo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.github.premnirmal.ticker.repo.data.HoldingRow
import com.github.premnirmal.ticker.repo.data.PropertiesRow
import com.github.premnirmal.ticker.repo.data.QuoteRow
import com.github.premnirmal.ticker.repo.data.QuoteWithHoldings

@Dao
interface QuoteDao {

  @Transaction
  @Query("SELECT * FROM QuoteRow")
  suspend fun getQuotesWithHoldings(): List<QuoteWithHoldings>

  @Transaction
  @Query("SELECT * FROM QuoteRow where symbol = :symbol")
  suspend fun getQuoteWithHoldings(symbol: String): QuoteWithHoldings?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  @JvmSuppressWildcards
  suspend fun upsertQuotes(quotes: List<QuoteRow>): LongArray

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  @JvmSuppressWildcards
  suspend fun upsertQuote(quote: QuoteRow): Long

  @Transaction
  suspend fun upsertQuoteAndHolding(
    quote: QuoteRow,
    holdings: List<HoldingRow>?
  ) {
    upsertQuote(quote)
    holdings?.let { upsertHoldings(quote.symbol, it) }
  }

  @Transaction
  suspend fun deleteQuoteAndHoldings(symbol: String) {
    deleteQuoteById(symbol)
    deleteHoldingsByQuoteId(symbol)
  }

  @Transaction
  suspend fun deleteQuotesAndHoldings(symbols: List<String>) {
    deleteByQuotesId(symbols)
    deleteHoldingsByQuoteIds(symbols)
  }

  @Query("DELETE FROM QuoteRow WHERE symbol = :symbol")
  suspend fun deleteQuoteById(symbol: String)

  @Transaction
  suspend fun upsertHoldings(
    symbol: String,
    holdings: List<HoldingRow>
  ) {
    deleteHoldingsByQuoteId(symbol)
    insertHoldings(holdings)
  }

  @Insert
  @JvmSuppressWildcards
  suspend fun insertHoldings(holdings: List<HoldingRow>): LongArray

  @Insert
  suspend fun insertHolding(holding: HoldingRow): Long

  @Query("DELETE FROM HoldingRow WHERE quote_symbol = :symbol")
  suspend fun deleteHoldingsByQuoteId(symbol: String)

  @Query("DELETE FROM QuoteRow WHERE symbol IN (:symbols)")
  suspend fun deleteByQuotesId(symbols: List<String>)

  @Query("DELETE FROM HoldingRow WHERE quote_symbol IN (:symbols)")
  suspend fun deleteHoldingsByQuoteIds(symbols: List<String>)

  @Delete
  suspend fun deleteHolding(holding: HoldingRow)

  @Transaction
  suspend fun upsertProperties(
    propertiesRow: PropertiesRow
  ) {
    if (propertiesRow.quoteSymbol.isNotEmpty()) {
      deletePropertiesByQuoteId(propertiesRow.quoteSymbol)
    }
    if (propertiesRow.notes.isNotEmpty() || propertiesRow.displayname.isNotEmpty() || propertiesRow.alertAbove > 0.0f || propertiesRow.alertBelow > 0.0f) {
      insertProperties(propertiesRow)
    }
  }

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  @JvmSuppressWildcards
  suspend fun insertProperties(quote: PropertiesRow)

  @Query("DELETE FROM PropertiesRow WHERE properties_quote_symbol = :symbol")
  suspend fun deletePropertiesByQuoteId(symbol: String)
}