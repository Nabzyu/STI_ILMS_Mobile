package com.example.stismartlib.ui.dashboard

sealed class DashboardRoute(val route: String) {
    object Overview : DashboardRoute("Dashboard")
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
    object ReserveBook : DashboardRoute("reserve_book")
    object Notifications : DashboardRoute("notifications")
    object QrLogin : DashboardRoute("qr_login")
    object PrintingService : DashboardRoute("printing_service")
    object PrintingHistory : DashboardRoute("printing_history")
    object Fines : DashboardRoute("fines")
    object Clearance : DashboardRoute("clearance")
    object Profile : DashboardRoute("profile")
    object Search : DashboardRoute("search")
    object FloorPlan : DashboardRoute("floor_plan")
    object MyBooks : DashboardRoute("my_books")
    object BookLocation : DashboardRoute("book_location/{titleId}") {
        fun create(titleId: String) = "book_location/$titleId"
    }
}
