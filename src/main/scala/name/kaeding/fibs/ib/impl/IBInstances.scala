package name.kaeding.fibs.ib.impl

import scalaz._, Scalaz._
import com.ib.client.{Order => IBOrder, _}

trait IBInstances {
  implicit def ContractShows: Show[Contract] = Show.shows(c => s"""Contract(
m_conId: ${c.m_conId},
m_symbol: ${c.m_symbol},
m_secType: ${c.m_secType},
m_expiry: ${c.m_expiry},
m_strike: ${c.m_strike},
m_right: ${c.m_right},
m_multiplier: ${c.m_multiplier},
m_exchange: ${c.m_exchange},
m_currency: ${c.m_currency},
m_localSymbol: ${c.m_localSymbol},
m_primaryExch: ${c.m_primaryExch},
m_includeExpired: ${c.m_includeExpired},
m_secIdType: ${c.m_secIdType},
m_secId: ${c.m_secId},
m_comboLegsDescrip: ${c.m_comboLegsDescrip},
m_comboLegs: ${c.m_comboLegs},
m_underComp: ${c.m_underComp})
""") 

  implicit def OrderShows: Show[IBOrder] = Show.shows(o => s"""Order(
// main order fields
m_orderId: ${o.m_orderId},
m_clientId: ${o.m_clientId},
m_permId: ${o.m_permId},
m_action: ${o.m_action},
m_totalQuantity: ${o.m_totalQuantity},
m_orderType: ${o.m_orderType},
m_lmtPrice: ${o.m_lmtPrice},
m_auxPrice: ${o.m_auxPrice},

// extended order fields
m_tif: ${o.m_tif},
m_ocaGroup: ${o.m_ocaGroup},
m_ocaType: ${o.m_ocaType},
m_orderRef: ${o.m_orderRef},
m_transmit: ${o.m_transmit},
m_parentId: ${o.m_parentId},
m_blockOrder: ${o.m_blockOrder},
m_sweepToFill: ${o.m_sweepToFill},
m_displaySize: ${o.m_displaySize},
m_triggerMethod: ${o.m_triggerMethod},
m_outsideRth: ${o.m_outsideRth},
m_hidden: ${o.m_hidden},
m_goodAfterTime: ${o.m_goodAfterTime},
m_goodTillDate: ${o.m_goodTillDate},
m_overridePercentageConstraints: ${o.m_overridePercentageConstraints},
m_rule80A: ${o.m_rule80A},
m_allOrNone: ${o.m_allOrNone},
m_minQty: ${o.m_minQty},
m_percentOffset: ${o.m_percentOffset},
m_trailStopPrice: ${o.m_trailStopPrice},
m_trailingPercent: ${o.m_trailingPercent},

// Financial advisors only 
m_faGroup: ${o.m_faGroup},
m_faProfile: ${o.m_faProfile},
m_faMethod: ${o.m_faMethod},
m_faPercentage: ${o.m_faPercentage},

// Institutional orders only
m_openClose: ${o.m_openClose},
m_origin: ${o.m_origin},
m_shortSaleSlot: ${o.m_shortSaleSlot},
m_designatedLocation: ${o.m_designatedLocation},
m_exemptCode: ${o.m_exemptCode},

// SMART routing only
m_discretionaryAmt: ${o.m_discretionaryAmt},
m_eTradeOnly: ${o.m_eTradeOnly},
m_firmQuoteOnly: ${o.m_firmQuoteOnly},
m_nbboPriceCap: ${o.m_nbboPriceCap},
m_optOutSmartRouting: ${o.m_optOutSmartRouting},

// BOX or VOL ORDERS ONLY
m_auctionStrategy: ${o.m_auctionStrategy},

// BOX ORDERS ONLY
m_startingPrice: ${o.m_startingPrice},
m_stockRefPrice: ${o.m_stockRefPrice},
m_delta: ${o.m_delta},

// pegged to stock or VOL orders
m_stockRangeLower: ${o.m_stockRangeLower},
m_stockRangeUpper: ${o.m_stockRangeUpper},

// VOLATILITY ORDERS ONLY
m_volatility: ${o.m_volatility},
m_volatilityType: ${o.m_volatilityType},
m_continuousUpdate: ${o.m_continuousUpdate},
m_referencePriceType: ${o.m_referencePriceType},
m_deltaNeutralOrderType: ${o.m_deltaNeutralOrderType},
m_deltaNeutralAuxPrice: ${o.m_deltaNeutralAuxPrice},
m_deltaNeutralConId: ${o.m_deltaNeutralConId},
m_deltaNeutralSettlingFirm: ${o.m_deltaNeutralSettlingFirm},
m_deltaNeutralClearingAccount: ${o.m_deltaNeutralClearingAccount},
m_deltaNeutralClearingIntent: ${o.m_deltaNeutralClearingIntent},
m_deltaNeutralOpenClose: ${o.m_deltaNeutralOpenClose},
m_deltaNeutralShortSale: ${o.m_deltaNeutralShortSale},
m_deltaNeutralShortSaleSlot: ${o.m_deltaNeutralShortSaleSlot},
m_deltaNeutralDesignatedLocation: ${o.m_deltaNeutralDesignatedLocation},

// COMBO ORDERS ONLY
m_basisPoints: ${o.m_basisPoints},
m_basisPointsType: ${o.m_basisPointsType},
    
// SCALE ORDERS ONLY
m_scaleInitLevelSize: ${o.m_scaleInitLevelSize},
m_scaleSubsLevelSize: ${o.m_scaleSubsLevelSize},
m_scalePriceIncrement: ${o.m_scalePriceIncrement},
m_scalePriceAdjustValue: ${o.m_scalePriceAdjustValue},
m_scalePriceAdjustInterval: ${o.m_scalePriceAdjustInterval},
m_scaleProfitOffset: ${o.m_scaleProfitOffset},
m_scaleAutoReset: ${o.m_scaleAutoReset},
m_scaleInitPosition: ${o.m_scaleInitPosition},
m_scaleInitFillQty: ${o.m_scaleInitFillQty},
m_scaleRandomPercent: ${o.m_scaleRandomPercent},

// HEDGE ORDERS ONLY
m_hedgeType: ${o.m_hedgeType},
m_hedgeParam: ${o.m_hedgeParam},

// Clearing info
m_account: ${o.m_account},
m_settlingFirm: ${o.m_settlingFirm},
m_clearingAccount: ${o.m_clearingAccount},
m_clearingIntent: ${o.m_clearingIntent},
    
// ALGO ORDERS ONLY
m_algoStrategy: ${o.m_algoStrategy},
m_algoParams: ${o.m_algoParams},

// What-if
m_whatIf: ${o.m_whatIf},

// Not Held
m_notHeld: ${o.m_notHeld},

// Smart combo routing params
m_smartComboRoutingParams: m_smartComboRoutingParams,
    
// order combo legs
m_orderComboLegs: ${o.m_orderComboLegs})
""")

  implicit def OrderStateShows: Show[OrderState] = Show.shows(s => s"""Order(
m_status: ${s.m_status},
m_initMargin: ${s.m_initMargin},
m_maintMargin: ${s.m_maintMargin},
m_equityWithLoan: ${s.m_equityWithLoan},
m_commission: ${s.m_commission},
m_minCommission: ${s.m_minCommission},
m_maxCommission: ${s.m_maxCommission},
m_commissionCurrency: ${s.m_commissionCurrency},
m_warningText: ${s.m_warningText})
""")
}