import SwiftUI

struct BankConnectView: View {
    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 20) {
                Text("Connect your bank")
                    .font(.title2)
                    .fontWeight(.bold)

                VStack(alignment: .leading, spacing: 12) {
                    BankOptionRow(name: "Sparkasse", subtitle: "Open Banking ready")
                    BankOptionRow(name: "Commerzbank", subtitle: "Secure connection")
                    BankOptionRow(name: "CSV import", subtitle: "Manual import fallback")
                }

                Spacer()

                Button {
                    // Replace with Plaid, Tink, FinAPI or bank-specific integration.
                } label: {
                    Text("Continue")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.blue)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding()
            .navigationTitle("Bank connection")
        }
    }
}

private struct BankOptionRow: View {
    let name: String
    let subtitle: String

    var body: some View {
        HStack {
            Image(systemName: "banknote.fill")
                .frame(width: 24)
            VStack(alignment: .leading) {
                Text(name)
                    .fontWeight(.semibold)
                Text(subtitle)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .foregroundStyle(.secondary)
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

#Preview {
    BankConnectView()
}
