package ma.projet.restclient;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ma.projet.restclient.adapter.CompteAdapter;
import ma.projet.restclient.entities.Compte;
import ma.projet.restclient.repository.CompteRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements CompteAdapter.OnDeleteClickListener, CompteAdapter.OnUpdateClickListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private CompteAdapter adapter;
    private RadioGroup formatGroup;
    private FloatingActionButton addbtn;
    private String currentFormat = "JSON";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            initViews();
            setupRecyclerView();
            setupFormatSelection();
            setupAddButton();

            loadData(currentFormat);
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: ", e);
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        try {
            recyclerView = findViewById(R.id.recyclerView);
            formatGroup = findViewById(R.id.formatGroup);
            addbtn = findViewById(R.id.fabAdd);

            if (recyclerView == null) {
                throw new RuntimeException("RecyclerView not found in layout");
            }
            if (formatGroup == null) {
                throw new RuntimeException("RadioGroup not found in layout");
            }
            if (addbtn == null) {
                throw new RuntimeException("FloatingActionButton not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in initViews: ", e);
            throw e;
        }
    }

    private void setupRecyclerView() {
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new CompteAdapter(this, this);
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error in setupRecyclerView: ", e);
            throw e;
        }
    }

    private void setupFormatSelection() {
        try {
            formatGroup.setOnCheckedChangeListener((group, checkedId) -> {
                try {
                    currentFormat = checkedId == R.id.radioJson ? "JSON" : "XML";
                    Log.d(TAG, "Format changed to: " + currentFormat);
                    loadData(currentFormat);
                } catch (Exception e) {
                    Log.e(TAG, "Error in format change: ", e);
                    showToast("Erreur lors du changement de format");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupFormatSelection: ", e);
            throw e;
        }
    }

    private void setupAddButton() {
        try {
            addbtn.setOnClickListener(v -> {
                try {
                    showAddCompteDialog();
                } catch (Exception e) {
                    Log.e(TAG, "Error showing add dialog: ", e);
                    showToast("Erreur lors de l'ouverture du dialogue");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in setupAddButton: ", e);
            throw e;
        }
    }

    private void showAddCompteDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

            EditText etSolde = dialogView.findViewById(R.id.etSolde);
            RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

            builder.setView(dialogView)
                    .setTitle("Ajouter un compte")
                    .setPositiveButton("Ajouter", (dialog, which) -> {
                        try {
                            String soldeStr = etSolde.getText().toString().trim();

                            if (soldeStr.isEmpty()) {
                                showToast("Veuillez saisir un solde");
                                return;
                            }

                            double solde = Double.parseDouble(soldeStr);
                            String type = typeGroup.getCheckedRadioButtonId() == R.id.radioCourant
                                    ? "COURANT" : "EPARGNE";

                            String formattedDate = getCurrentDateFormatted();
                            Compte compte = new Compte(null, solde, type, formattedDate);
                            addCompte(compte);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid number format: ", e);
                            showToast("Solde invalide");
                        } catch (Exception e) {
                            Log.e(TAG, "Error adding compte: ", e);
                            showToast("Erreur lors de l'ajout");
                        }
                    })
                    .setNegativeButton("Annuler", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showAddCompteDialog: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    private String getCurrentDateFormatted() {
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return formatter.format(calendar.getTime());
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: ", e);
            return "2025-01-01";
        }
    }

    private void addCompte(Compte compte) {
        try {
            Log.d(TAG, "Adding compte: " + compte.toString());
            CompteRepository compteRepository = new CompteRepository("JSON");
            compteRepository.addCompte(compte, new Callback<Compte>() {
                @Override
                public void onResponse(Call<Compte> call, Response<Compte> response) {
                    Log.d(TAG, "Add response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        showToast("Compte ajouté avec succès");
                        loadData(currentFormat);
                    } else {
                        Log.e(TAG, "Add failed with code: " + response.code());
                        showToast("Erreur lors de l'ajout: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Compte> call, Throwable t) {
                    Log.e(TAG, "Add failed: ", t);
                    showToast("Erreur: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in addCompte: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    private void loadData(String format) {
        try {
            Log.d(TAG, "Loading data with format: " + format);
            CompteRepository compteRepository = new CompteRepository(format);
            compteRepository.getAllCompte(new Callback<List<Compte>>() {
                @Override
                public void onResponse(Call<List<Compte>> call, Response<List<Compte>> response) {
                    Log.d(TAG, "Load response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        List<Compte> comptes = response.body();
                        Log.d(TAG, "Loaded " + comptes.size() + " comptes");
                        runOnUiThread(() -> {
                            try {
                                adapter.updateData(comptes);
                                if (comptes.isEmpty()) {
                                    showToast("Aucun compte trouvé");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error updating adapter: ", e);
                                showToast("Erreur lors de la mise à jour");
                            }
                        });
                    } else {
                        Log.e(TAG, "Load failed with code: " + response.code());
                        showToast("Erreur de chargement: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<Compte>> call, Throwable t) {
                    Log.e(TAG, "Load failed: ", t);
                    showToast("Erreur: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadData: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateClick(Compte compte) {
        try {
            showUpdateCompteDialog(compte);
        } catch (Exception e) {
            Log.e(TAG, "Error showing update dialog: ", e);
            showToast("Erreur lors de l'ouverture du dialogue");
        }
    }

    private void showUpdateCompteDialog(Compte compte) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_compte, null);

            EditText etSolde = dialogView.findViewById(R.id.etSolde);
            RadioGroup typeGroup = dialogView.findViewById(R.id.typeGroup);

            etSolde.setText(String.valueOf(compte.getSolde()));

            if (compte.getType() != null && compte.getType().equalsIgnoreCase("COURANT")) {
                typeGroup.check(R.id.radioCourant);
            } else if (compte.getType() != null && compte.getType().equalsIgnoreCase("EPARGNE")) {
                typeGroup.check(R.id.radioEpargne);
            }

            builder.setView(dialogView)
                    .setTitle("Modifier le compte #" + compte.getId())
                    .setPositiveButton("Modifier", (dialog, which) -> {
                        try {
                            String soldeStr = etSolde.getText().toString().trim();

                            if (soldeStr.isEmpty()) {
                                showToast("Veuillez saisir un solde");
                                return;
                            }

                            double solde = Double.parseDouble(soldeStr);
                            String type = typeGroup.getCheckedRadioButtonId() == R.id.radioCourant
                                    ? "COURANT" : "EPARGNE";

                            compte.setSolde(solde);
                            compte.setType(type);
                            updateCompte(compte);
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Invalid number format: ", e);
                            showToast("Solde invalide");
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating compte: ", e);
                            showToast("Erreur lors de la modification");
                        }
                    })
                    .setNegativeButton("Annuler", null);

            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showUpdateCompteDialog: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    private void updateCompte(Compte compte) {
        try {
            Log.d(TAG, "Updating compte: " + compte.toString());
            CompteRepository compteRepository = new CompteRepository("JSON");
            compteRepository.updateCompte(compte.getId(), compte, new Callback<Compte>() {
                @Override
                public void onResponse(Call<Compte> call, Response<Compte> response) {
                    Log.d(TAG, "Update response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        showToast("Compte modifié avec succès");
                        loadData(currentFormat);
                    } else {
                        Log.e(TAG, "Update failed with code: " + response.code());
                        showToast("Erreur lors de la modification: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Compte> call, Throwable t) {
                    Log.e(TAG, "Update failed: ", t);
                    showToast("Erreur: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateCompte: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    @Override
    public void onDeleteClick(Compte compte) {
        try {
            showDeleteConfirmationDialog(compte);
        } catch (Exception e) {
            Log.e(TAG, "Error showing delete dialog: ", e);
            showToast("Erreur lors de l'ouverture du dialogue");
        }
    }

    private void showDeleteConfirmationDialog(Compte compte) {
        try {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Voulez-vous vraiment supprimer le compte #" + compte.getId() + " ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        try {
                            deleteCompte(compte);
                        } catch (Exception e) {
                            Log.e(TAG, "Error deleting compte: ", e);
                            showToast("Erreur lors de la suppression");
                        }
                    })
                    .setNegativeButton("Non", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error in showDeleteConfirmationDialog: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    private void deleteCompte(Compte compte) {
        try {
            Log.d(TAG, "Deleting compte: " + compte.getId());
            CompteRepository compteRepository = new CompteRepository("JSON");
            compteRepository.deleteCompte(compte.getId(), new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Log.d(TAG, "Delete response code: " + response.code());
                    if (response.isSuccessful()) {
                        showToast("Compte supprimé avec succès");
                        loadData(currentFormat);
                    } else {
                        Log.e(TAG, "Delete failed with code: " + response.code());
                        showToast("Erreur lors de la suppression: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Delete failed: ", t);
                    showToast("Erreur: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteCompte: ", e);
            showToast("Erreur: " + e.getMessage());
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            try {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing toast: ", e);
            }
        });
    }
}