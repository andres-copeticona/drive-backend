create sequence documentos_documentoid_seq
    as integer;

alter sequence documentos_documentoid_seq owner to postgres;

create table etiquetas
(
    etiquetasid     serial
        constraint etiquetas_pk
            primary key,
    nombreetiquetas varchar(200) not null,
    createdat       timestamp    not null
);

alter table etiquetas
    owner to postgres;

create table carpetasetiquetas
(
    carpetas_folderid     integer not null,
    etiquetas_etiquetasid integer not null
        constraint carpetasetiquetas_etiquetas
            references etiquetas
);

alter table carpetasetiquetas
    owner to postgres;

create table eventos
(
    eventid           serial
        constraint eventos_pk
            primary key,
    tipoevento        varchar(250) not null,
    descripcionevento varchar(250) not null,
    createdat         timestamp    not null
);

alter table eventos
    owner to postgres;

create table notificaciones_usuarios
(
    notificacionid    serial
        constraint notificaciones_usuarios_pk
            primary key,
    eventos_eventid   integer   not null
        constraint notificaciones_usuarios_eventos
            references eventos,
    usuario_usuarioid integer   not null,
    leido             boolean   not null,
    createdat         timestamp not null
);

alter table notificaciones_usuarios
    owner to postgres;

create table rol
(
    rolid      bigserial
        primary key,
    nombre_rol varchar(255) not null
);

alter table rol
    owner to postgres;

create table usuario
(
    usuarioid  bigserial
        primary key,
    celular    varchar(255),
    created_at timestamp(6),
    deleted    boolean not null,
    domicilio  varchar(255),
    edad       varchar(255),
    email      varchar(255),
    genero     varchar(255),
    materno    varchar(255),
    nombre     varchar(255),
    password   varchar(255),
    paterno    varchar(255),
    status     boolean not null,
    updated_at timestamp(6),
    usuario    varchar(255),
    rolid      bigint  not null
        constraint fk3rjdui1edm7erfvse8hdcpdac
            references rol
);

alter table usuario
    owner to postgres;

create table documentos
(
    documentoid bigint default nextval('documentos_documentoid_seq'::regclass) not null
        constraint documentos_pk
            primary key,
    folderid    bigint                                                         not null,
    usuarioid   bigint                                                         not null
        constraint fkguwf9192ah9knbmp7m04e5c70
            references usuario,
    titulo      varchar(200)                                                   not null,
    descripcion varchar(200)                                                   not null,
    filepath    varchar(500)                                                   not null,
    tipoacceso  varchar(50)                                                    not null,
    createdat   timestamp                                                      not null,
    update      timestamp                                                      not null,
    tipo_acceso varchar(255),
    updated_at  timestamp(6)                                                   not null
);

alter table documentos
    owner to postgres;

alter sequence documentos_documentoid_seq owned by documentos.documentoid;

create table documento_etiquetas
(
    documentos_documentoid integer not null
        constraint documento_etiquetas_documentos
            references documentos,
    etiquetas_etiquetasid  integer not null
        constraint documento_etiquetas_etiquetas
            references etiquetas
);

alter table documento_etiquetas
    owner to postgres;

create table documentos_compartidos
(
    compartidoid       serial
        constraint documentos_compartidos_pk
            primary key,
    receptor_usuarioid integer   not null,
    emisor_usuarioid   integer   not null,
    documentoid        integer   not null
        constraint permisos_documentos_documentos
            references documentos,
    tipoacceso         integer   not null,
    createdat          timestamp not null
);

alter table documentos_compartidos
    owner to postgres;

create table versiones_documentos
(
    documentoversionesid serial
        constraint versiones_documentos_pk
            primary key,
    documentoid          integer      not null
        constraint versiones_documentos_documentos
            references documentos,
    numeroversion        integer      not null,
    filepath             varchar(200) not null,
    createdat            timestamp    not null
);

alter table versiones_documentos
    owner to postgres;

create table s3_object
(
    s3_object_id bigserial
        primary key,
    bucket       varchar(255),
    content_type varchar(255),
    filename     varchar(255),
    status       boolean
);

alter table s3_object
    owner to postgres;

create table carpetas
(
    folderid    bigserial
        primary key,
    createdat   timestamp(6) not null,
    nombre      varchar(255) not null,
    tipo_acceso varchar(255) not null,
    updatedat   timestamp(6) not null,
    usuarioid   bigint       not null
        constraint fkct2m3x47evq70pfaaow2vj2wd
            references usuario
);

alter table carpetas
    owner to postgres;


