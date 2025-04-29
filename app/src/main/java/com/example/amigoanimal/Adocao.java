package com.example.amigoanimal;

import com.google.firebase.firestore.PropertyName;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Adocao {

    private String nome_adotante;
    private String email_adotante;
    private String telefone_adotante;
    private String dados_animal;
    private String tipo_animal;
    private String data_adocao;
    private String data_criacao;
    private String profissional;
    private ArrayList<String> pesquisa;
    private String sexo_animal, nome_animal, idade_animal, localizacao_adotante;
    private List<String> imagens;
    private List<String> documentos;

    public Adocao() {
        // Construtor vazio necessário para Firebase
    }

    @PropertyName("nome_adotante")
    public String getNomeAdotante() { return nome_adotante; }

    @PropertyName("nome_adotante")
    public void setNomeAdotante(String nome_adotante) { this.nome_adotante = nome_adotante; }

    @PropertyName("email_adotante")
    public String getEmailAdotante() { return email_adotante; }

    @PropertyName("email_adotante")
    public void setEmailAdotante(String email_adotante) { this.email_adotante = email_adotante; }

    @PropertyName("telefone_adotante")
    public String getTelefoneAdotante() { return telefone_adotante; }

    @PropertyName("telefone_adotante")
    public void setTelefoneAdotante(String telefone_adotante) { this.telefone_adotante = telefone_adotante; }

    @PropertyName("dados_animal")
    public String getDadosAnimal() { return dados_animal; }

    @PropertyName("dados_animal")
    public void setDadosAnimal(String dados_animal) { this.dados_animal = dados_animal; }

    @PropertyName("profissional")
    public String getProfissional() { return profissional; }

    @PropertyName("profissional")
    public void setProfissional(String profissional) { this.profissional = profissional; }

    @PropertyName("sexo_animal")
    public String getSexoAnimal() { return sexo_animal; }

    @PropertyName("sexo_animal")
    public void setSexoAnimal(String sexo_animal) { this.sexo_animal = sexo_animal; }

    @PropertyName("nome_animal")
    public String getNomeAnimal() { return nome_animal; }

    @PropertyName("nome_animal")
    public void setNomeAnimal(String nome_animal) { this.nome_animal = nome_animal; }

    @PropertyName("idade_animal")
    public String getIdadeAnimal() { return idade_animal; }

    @PropertyName("idade_animal")
    public void setIdadeAnimal(String idade_animal) { this.idade_animal = idade_animal; }

    @PropertyName("pesquisa")
    public ArrayList<String> getPesquisa() { return pesquisa; }

    @PropertyName("pesquisa")
    public void setPesquisa(ArrayList<String> pesquisa) { this.pesquisa = pesquisa; }

    @PropertyName("localizacao_adotante")
    public String getLocalizacaoAdotante() { return localizacao_adotante; }

    @PropertyName("localizacao_adotante")
    public void setLocalizacaoAdotante(String localizacao_adotante) { this.localizacao_adotante = localizacao_adotante; }

    @PropertyName("data_criacao")
    public String getDataCriacao() { return data_criacao; }

    @PropertyName("data_criacao")
    public void setDataCriacao(String data_criacao) { this.data_criacao = data_criacao; }
    @PropertyName("tipo_animal")
    public String getTipoAnimal() { return tipo_animal; }

    @PropertyName("tipo_animal")
    public void setTipoAnimal(String tipo_animal) { this.tipo_animal = tipo_animal; }

    @PropertyName("data_adocao")
    public String getDataAdocao() { return data_adocao; }

    @PropertyName("data_adocao")
    public void setDataAdocao(String data_adocao) { this.data_adocao = data_adocao; }

    @PropertyName("imagens")
    public List<String> getImagens() { return imagens; }

    @PropertyName("imagens")
    public void setImagens(List<String> imagens) { this.imagens = imagens; }

    @PropertyName("documentos")
    public List<String> getDocumentos() { return documentos; }

    @PropertyName("documentos")
    public void setDocumentos(List<String> documentos) { this.documentos = documentos; }
}
