package com.example.stismartlib.ui.dashboard

sealed class DashboardRoute(val route: String) {
    object Overview : DashboardRoute("overview")
    object BookCatalog : DashboardRoute("book_catalog")
    object BookDetail : DashboardRoute("book_detail/{bookId}") {
        fun create(bookId: String) = "book_detail/$bookId"
    }
    object BookCart : DashboardRoute("book_cart")
    object ResearchCatalog : DashboardRoute("research_catalog")
    object ResearchDetail : DashboardRoute("research_detail/{researchId}") {
        fun create(researchId: String) = "research_detail/$researchId"
    }
    object BorrowingHistory : DashboardRoute("borrowing_history")
    object BorrowProcess : DashboardRoute("borrow_process")
    object ReturnBook : DashboardRoute("return_book")
    object ReserveBook : DashboardRoute("reserve_book")
    object Notifications : DashboardRoute("notifications")
    object QrLogin : DashboardRoute("qr_login")
    object PrintingService : DashboardRoute("printing_service")
    object PrintingHistory : DashboardRoute("printing_history")
    object Fines : DashboardRoute("fines")
    object Clearance : DashboardRoute("clearance")
}
