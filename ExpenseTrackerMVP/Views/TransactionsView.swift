import SwiftUI

struct TransactionsView: View {
    @EnvironmentObject private var store: ExpenseStore

    var body: some View {
        NavigationStack {
            List {
                ForEach(store.transactions.sorted { $0.date > $1.date }) { transaction in
                    HStack(alignment: .center, spacing: 12) {
                        Circle()
                            .fill(transaction.category.color)
                            .frame(width: 12, height: 12)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(transaction.name)
                                .font(.headline)
                            Text(transaction.merchant)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }

                        Spacer()

                        VStack(alignment: .trailing, spacing: 4) {
                            Text("\(transaction.isIncoming ? "+" : "-")€\(String(format: "%.2f", abs(transaction.amount)))")
                                .fontWeight(.semibold)
                                .foregroundStyle(transaction.isIncoming ? .green : .primary)
                            Text(transaction.date.formatted(date: .numeric, time: .omitted))
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
            .navigationTitle("Transactions")
        }
    }
}

#Preview {
    TransactionsView()
        .environmentObject(ExpenseStore())
}
