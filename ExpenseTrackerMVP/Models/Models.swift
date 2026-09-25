import Foundation
import SwiftUI

enum ExpenseCategory: String, CaseIterable, Identifiable {
    case food = "Food"
    case housing = "Housing"
    case transport = "Transport"
    case shopping = "Shopping"
    case leisure = "Leisure"
    case bills = "Bills"
    case other = "Other"

    var id: String { rawValue }

    var color: Color {
        switch self {
        case .food: return .orange
        case .housing: return .blue
        case .transport: return .green
        case .shopping: return .purple
        case .leisure: return .pink
        case .bills: return .red
        case .other: return .gray
        }
    }
}

struct ExpenseTransaction: Identifiable, Equatable {
    let id: UUID
    let name: String
    let category: ExpenseCategory
    let amount: Double
    let date: Date
    let merchant: String
    let isIncoming: Bool

    var signedAmount: Double {
        isIncoming ? amount : -amount
    }

    var formattedAmount: String {
        let value = abs(amount)
        return String(format: "€%.2f", value)
    }
}

struct Budget: Identifiable {
    let id: UUID
    let category: ExpenseCategory
    let limit: Double
    let spent: Double

    var progress: Double {
        limit <= 0 ? 0 : min(spent / limit, 1)
    }

    var remaining: Double {
        limit - spent
    }
}
