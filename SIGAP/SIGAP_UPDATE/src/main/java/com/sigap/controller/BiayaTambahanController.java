package com.sigap.controller;

import com.sigap.ADT.BiayaTambahan;
import com.sigap.APP.CRUD_BiayaTambahan;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class BiayaTambahanController implements Initializable {

    @FXML
    private Button btnBersih;
    @FXML
    private MenuButton btnFilter;
    @FXML
    private Button btnHapus;
    @FXML
    private Button btnSimpan;
    @FXML
    private Button btnUbah;
    @FXML
    private TableColumn<BiayaTambahan, String> colId;
    @FXML
    private TableColumn<BiayaTambahan, String> coljenispelanggaran;
    @FXML
    private TableColumn<BiayaTambahan, String> colketerangan;
    @FXML
    private TableColumn<BiayaTambahan, Double> colnominal;
    @FXML
    private TableColumn<BiayaTambahan, String> colstatus;
    @FXML
    private Label lblPage;
    @FXML
    private Label lblTotal;
    @FXML
    private MenuItem miResetFilter;
    @FXML
    private RadioButton rbKerusakanFasilitas;
    @FXML
    private RadioButton rbKeterlambatanBayarSewa;
    @FXML
    private RadioMenuItem rmiHargaTermahal;
    @FXML
    private RadioMenuItem rmiHargaTermurah;
    @FXML
    private RadioMenuItem rmiJenisKerusakan;
    @FXML
    private RadioMenuItem rmiJenisTerlambat;
    @FXML
    private RadioMenuItem rmiNamaAZ;
    @FXML
    private RadioMenuItem rmiNamaZA;
    @FXML
    private RadioMenuItem rmiStatusAktif;
    @FXML
    private RadioMenuItem rmiStatusTidakAktif;
    @FXML
    private TableView<BiayaTambahan> tabelBiayaTambahan;
    @FXML
    private ToggleGroup tgFilterHarga;
    @FXML
    private ToggleGroup tgFilterJenis;
    @FXML
    private ToggleGroup tgFilterNama;
    @FXML
    private ToggleGroup tgFilterStatus;
    @FXML
    private ToggleGroup tgJenis;
    @FXML
    private TextField txtBiayaTambahan;
    @FXML
    private TextArea txtKeterangan;
    @FXML
    private TextField txtNominal;
    @FXML
    private TextField txtcari;

    private static final String KERUSAKAN = "Kerusakan Fasilitas";
    private static final String KETERLAMBATAN = "Keterlambatan Bayar Sewa";
    private static final String AKTIF = "Aktif";
    private static final String TIDAK_AKTIF = "Tidak Aktif";
    private static final NumberFormat RIBUAN_FORMAT = NumberFormat.getInstance(new Locale("in", "ID"));


    private final ObservableList<BiayaTambahan> masterList = FXCollections.observableArrayList();

    private final ObservableList<BiayaTambahan> displayList = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private static final String STYLE_READONLY =
            "-fx-background-color:#F0F0F0;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;-fx-text-fill:#888;";
    private static final String STYLE_NORMAL =
            "-fx-background-color:WHITE;-fx-border-color:#D0D8E8;" +
                    "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:6 12;" +
                    "-fx-font-size:13px;";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtBiayaTambahan.setEditable(false);

        setupTable();
        setupNominalFormatter();
        setFormState(false, false);

        Platform.runLater(() -> {
            loadData();
            autoGenerateId();
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIdBiayaTambahan()));
        coljenispelanggaran.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getJenisBiayaTambahan()));
        colnominal.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(
                d.getValue().getNominalDenda()).asObject());
        colketerangan.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getKeterangan()));
        colstatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStsDenda()));

        colstatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                Label badge = new Label(val);
                badge.setStyle("Aktif".equalsIgnoreCase(val)
                        ? "-fx-background-color:#E0F5E8;-fx-text-fill:#1E8A3C;-fx-font-weight:700;" +
                        "-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;"
                        : "-fx-background-color:#FFE8E8;-fx-text-fill:#C0392B;-fx-font-weight:700;" +
                        "-fx-font-size:11px;-fx-padding:3 10;-fx-background-radius:10;");
                setGraphic(badge);
                setText(null);
            }
        });

        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) { setText(null); setStyle(""); return; }
                setText(id);
                setStyle("-fx-text-fill:#1A3A8F;-fx-font-weight:600;");
            }
        });

        colnominal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("Rp %,.0f", val));
            }
        });
    }

    private void loadData() {
        try {
            List<BiayaTambahan> list = CRUD_BiayaTambahan.getAll();
            masterList.setAll(list);
            applyFilters();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Koneksi",
                    "Gagal memuat data.\nDetail: " + e.getMessage());
        }
    }

    private void applyFilters() {
        List<BiayaTambahan> filtered = new ArrayList<>(masterList);

        if (rmiJenisKerusakan != null && rmiJenisKerusakan.isSelected()) {
            filtered.removeIf(d -> !KERUSAKAN.equalsIgnoreCase(d.getJenisBiayaTambahan()));
        } else if (rmiJenisTerlambat != null && rmiJenisTerlambat.isSelected()) {
            filtered.removeIf(d -> !KETERLAMBATAN.equalsIgnoreCase(d.getJenisBiayaTambahan()));
        }

        if (rmiStatusAktif != null && rmiStatusAktif.isSelected()) {
            filtered.removeIf(d -> !AKTIF.equalsIgnoreCase(
                    d.getStsDenda() != null ? d.getStsDenda().trim() : ""));
        } else if (rmiStatusTidakAktif != null && rmiStatusTidakAktif.isSelected()) {
            filtered.removeIf(d -> !TIDAK_AKTIF.equalsIgnoreCase(
                    d.getStsDenda() != null ? d.getStsDenda().trim() : ""));
        }

        if (rmiHargaTermahal != null && rmiHargaTermahal.isSelected()) {
            filtered.sort((a, b) -> Double.compare(b.getNominalDenda(), a.getNominalDenda()));
        } else if (rmiHargaTermurah != null && rmiHargaTermurah.isSelected()) {
            filtered.sort(Comparator.comparingDouble(BiayaTambahan::getNominalDenda));
        }

        if (rmiNamaAZ != null && rmiNamaAZ.isSelected()) {
            filtered.sort(Comparator.comparing(BiayaTambahan::getJenisBiayaTambahan, String.CASE_INSENSITIVE_ORDER));
        } else if (rmiNamaZA != null && rmiNamaZA.isSelected()) {
            filtered.sort(Comparator.comparing(BiayaTambahan::getJenisBiayaTambahan, String.CASE_INSENSITIVE_ORDER).reversed());
        }

        displayList.setAll(filtered);
        currentPage = 1;
        refreshTable();
    }

    private void refreshTable() {
        int total = displayList.size();
        totalPage = (total == 0) ? 1 : (int) Math.ceil((double) total / PAGE_SIZE);
        if (currentPage > totalPage) currentPage = totalPage;
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        tabelBiayaTambahan.setItems(FXCollections.observableArrayList(displayList.subList(from, to)));
        lblTotal.setText("Total Data : " + total);
        lblPage.setText(String.valueOf(currentPage));
    }

    private void autoGenerateId() {
        try {
            txtBiayaTambahan.setText(CRUD_BiayaTambahan.generateNextId());
        } catch (Exception e) {
            txtBiayaTambahan.setText("BY001");
        }
    }


    private void setupNominalFormatter() {
        txtNominal.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;

            String digits = newVal.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                if (!newVal.isEmpty()) txtNominal.setText("");
                return;
            }
            // buang angka nol di depan (kecuali angka itu sendiri "0")
            digits = digits.replaceFirst("^0+(?=\\d)", "");

            String formatted = formatRibuan(digits);
            if (!formatted.equals(newVal)) {
                txtNominal.setText(formatted);
                txtNominal.positionCaret(formatted.length());
            }
        });
    }


    private String formatRibuan(String digits) {
        try {
            long value = Long.parseLong(digits);
            return RIBUAN_FORMAT.format(value);
        } catch (NumberFormatException e) {
            return digits;
        }
    }

    private double parseNominal(String text) {
        String digits = text == null ? "" : text.replaceAll("[^\\d]", "");
        return digits.isEmpty() ? 0 : Double.parseDouble(digits);
    }


    private String getSelectedJenis() {
        if (rbKerusakanFasilitas.isSelected()) return KERUSAKAN;
        if (rbKeterlambatanBayarSewa.isSelected()) return KETERLAMBATAN;
        return null;
    }

    private void setSelectedJenis(String jenis) {
        if (KERUSAKAN.equalsIgnoreCase(jenis)) {
            rbKerusakanFasilitas.setSelected(true);
        } else if (KETERLAMBATAN.equalsIgnoreCase(jenis)) {
            rbKeterlambatanBayarSewa.setSelected(true);
        } else if (tgJenis != null) {
            tgJenis.selectToggle(null);
        }
    }

    private void setFormState(boolean editMode, boolean locked) {
        btnSimpan.setDisable(editMode || locked);
        btnUbah.setDisable(!editMode || locked);
        btnHapus.setDisable(!editMode || locked);

        rbKerusakanFasilitas.setDisable(locked);
        rbKeterlambatanBayarSewa.setDisable(locked);
        txtNominal.setEditable(!locked);
        txtKeterangan.setEditable(!locked);

        txtNominal.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
        txtKeterangan.setStyle(locked ? STYLE_READONLY : STYLE_NORMAL);
    }

    private boolean validasi() {
        StringBuilder sb = new StringBuilder();

        if (getSelectedJenis() == null) {
            sb.append("• Jenis Biaya Tambahan wajib dipilih (Kerusakan Fasilitas / Keterlambatan Bayar Sewa).\n");
        }

        String nominalStr = txtNominal.getText() == null ? "" : txtNominal.getText().trim();
        if (nominalStr.isEmpty()) {
            sb.append("• Nominal wajib diisi.\n");
        } else {
            try {
                double nominal = parseNominal(nominalStr);
                if (nominal <= 0) {
                    sb.append("• Nominal harus lebih besar dari 0.\n");
                }
            } catch (NumberFormatException e) {
                sb.append("• Nominal harus berupa angka.\n");
            }
        }

        String keterangan = txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim();
        if (keterangan.length() > 80) {
            sb.append("• Keterangan maksimal 80 karakter.\n");
        }

        if (sb.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validasi Input", sb.toString());
            return false;
        }
        return true;
    }

    private void bersihForm() {
        txtBiayaTambahan.clear();
        if (tgJenis != null) tgJenis.selectToggle(null);
        txtNominal.clear();
        txtKeterangan.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Runnable show = () -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            if (txtBiayaTambahan != null && txtBiayaTambahan.getScene() != null)
                alert.initOwner(txtBiayaTambahan.getScene().getWindow());
            alert.showAndWait();
        };
        if (Platform.isFxApplicationThread()) show.run();
        else Platform.runLater(show);
    }

    @FXML
    void onbersih(ActionEvent event) {
        bersihForm();
        setFormState(false, false);
        tabelBiayaTambahan.getSelectionModel().clearSelection();
        autoGenerateId();
        rbKerusakanFasilitas.requestFocus();
    }

    @FXML
    void onTableClick(MouseEvent event) {
        BiayaTambahan d = tabelBiayaTambahan.getSelectionModel().getSelectedItem();
        if (d == null) return;

        txtBiayaTambahan.setText(d.getIdBiayaTambahan());
        setSelectedJenis(d.getJenisBiayaTambahan());
        txtNominal.setText(formatRibuan(String.valueOf((long) d.getNominalDenda())));
        txtKeterangan.setText(d.getKeterangan());

        boolean isTidakAktif = "Tidak Aktif".equalsIgnoreCase(
                d.getStsDenda() != null ? d.getStsDenda().trim() : ""
        );
        setFormState(true, isTidakAktif);
    }

    @FXML
    void oncari(ActionEvent event) {
        String kw = txtcari.getText().trim();
        if (kw.isEmpty()) { loadData(); return; }
        try {
            List<BiayaTambahan> hasil = CRUD_BiayaTambahan.search(kw);
            masterList.setAll(hasil);
            applyFilters();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Cari", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onFirstPage(ActionEvent event) {
        currentPage = 1;
        refreshTable();
    }

    @FXML
    void onLastPage(ActionEvent event) {
        currentPage = totalPage;
        refreshTable();
    }

    @FXML
    void onNextPage(ActionEvent event) {
        if (currentPage < totalPage) { currentPage++; refreshTable(); }
    }

    @FXML
    void onPrevPage(ActionEvent event) {
        if (currentPage > 1) { currentPage--; refreshTable(); }
    }

    @FXML
    void onFilterJenisKerusakan(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterJenisTerlambat(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterStatusAktif(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterStatusTidakAktif(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterHargaTermahal(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterHargaTermurah(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterNamaAZ(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onFilterNamaZA(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void onResetFilter(ActionEvent event) {
        if (tgFilterJenis != null) tgFilterJenis.selectToggle(null);
        if (tgFilterStatus != null) tgFilterStatus.selectToggle(null);
        if (tgFilterHarga != null) tgFilterHarga.selectToggle(null);
        if (tgFilterNama != null) tgFilterNama.selectToggle(null);
        applyFilters();
    }

    @FXML
    void onhapus(ActionEvent event) {
        String id = txtBiayaTambahan.getText().trim();
        if (id.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih data biaya tambahan yang ingin dinonaktifkan.");
            return;
        }

        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Nonaktifkan");
        konfirmasi.setHeaderText("Nonaktifkan Biaya Tambahan");
        konfirmasi.setContentText("Data [" + id + "] akan diubah statusnya menjadi Tidak Aktif.\nLanjutkan?");
        if (txtBiayaTambahan.getScene() != null)
            konfirmasi.initOwner(txtBiayaTambahan.getScene().getWindow());

        Optional<ButtonType> result = konfirmasi.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                CRUD_BiayaTambahan.delete(id);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data berhasil dinonaktifkan.");
                loadData();
                onbersih(null);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Error: " + e.getMessage());
            }
        }
    }

    @FXML
    void onsimpan(ActionEvent event) {
        if (!validasi()) return;
        try {
            BiayaTambahan d = new BiayaTambahan(
                    txtBiayaTambahan.getText().trim(),
                    getSelectedJenis(),
                    parseNominal(txtNominal.getText()),
                    txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim()
            );
            CRUD_BiayaTambahan.insert(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data biaya tambahan berhasil disimpan.");
            loadData();
            onbersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Simpan", "Error: " + e.getMessage());
        }
    }

    @FXML
    void onubah(ActionEvent event) {
        if (!validasi()) return;
        try {
            BiayaTambahan d = new BiayaTambahan(
                    txtBiayaTambahan.getText().trim(),
                    getSelectedJenis(),
                    parseNominal(txtNominal.getText()),
                    txtKeterangan.getText() == null ? "" : txtKeterangan.getText().trim()
            );
            CRUD_BiayaTambahan.update(d);
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data biaya tambahan berhasil diubah.");
            loadData();
            onbersih(null);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Gagal Ubah", "Error: " + e.getMessage());
        }
    }
}