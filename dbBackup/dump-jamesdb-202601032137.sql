--
-- PostgreSQL database dump
--

-- Dumped from database version 17.7
-- Dumped by pg_dump version 17.0

-- Started on 2026-01-03 21:37:31

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 16439)
-- Name: hstore; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS hstore WITH SCHEMA public;


--
-- TOC entry 4623 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION hstore; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION hstore IS 'data type for storing sets of (key, value) pairs';


--
-- TOC entry 301 (class 1255 OID 16775)
-- Name: remove_elements_from_array(text[], text[]); Type: FUNCTION; Schema: public; Owner: dbmasteruser
--

CREATE FUNCTION public.remove_elements_from_array(source text[], elements_to_remove text[]) RETURNS text[]
    LANGUAGE plpgsql
    AS $$
DECLARE
    result text[];
BEGIN
    select array_agg(elements) INTO result
    from (select unnest(source)
          except
          select unnest(elements_to_remove)) t (elements);
    RETURN result;
END;
$$;


ALTER FUNCTION public.remove_elements_from_array(source text[], elements_to_remove text[]) OWNER TO dbmasteruser;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 224 (class 1259 OID 16623)
-- Name: attachment; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.attachment (
    id character varying NOT NULL,
    blob_id character varying,
    type character varying,
    message_id uuid,
    size bigint
);


ALTER TABLE public.attachment OWNER TO dbmasteruser;

--
-- TOC entry 243 (class 1259 OID 16756)
-- Name: custom_identity; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.custom_identity (
    username character varying(255) NOT NULL,
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    reply_to json NOT NULL,
    bcc json NOT NULL,
    text_signature character varying(255) NOT NULL,
    html_signature character varying(255) NOT NULL,
    sort_order integer NOT NULL,
    may_delete boolean NOT NULL
);


ALTER TABLE public.custom_identity OWNER TO dbmasteruser;

--
-- TOC entry 229 (class 1259 OID 16657)
-- Name: domains; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.domains (
    domain character varying NOT NULL
);


ALTER TABLE public.domains OWNER TO dbmasteruser;

--
-- TOC entry 239 (class 1259 OID 16728)
-- Name: email_change; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.email_change (
    account_id character varying NOT NULL,
    state uuid NOT NULL,
    date timestamp with time zone NOT NULL,
    is_shared boolean NOT NULL,
    created uuid[] NOT NULL,
    updated uuid[] NOT NULL,
    destroyed uuid[] NOT NULL
);


ALTER TABLE public.email_change OWNER TO dbmasteruser;

--
-- TOC entry 244 (class 1259 OID 16763)
-- Name: email_query_view; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.email_query_view (
    mailbox_id uuid NOT NULL,
    message_id uuid NOT NULL,
    received_at timestamp with time zone NOT NULL,
    sent_at timestamp with time zone NOT NULL
);


ALTER TABLE public.email_query_view OWNER TO dbmasteruser;

--
-- TOC entry 228 (class 1259 OID 16650)
-- Name: event_dead_letters; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.event_dead_letters (
    insertion_id uuid NOT NULL,
    "group" character varying NOT NULL,
    event character varying NOT NULL
);


ALTER TABLE public.event_dead_letters OWNER TO dbmasteruser;

--
-- TOC entry 234 (class 1259 OID 16692)
-- Name: event_store; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.event_store (
    aggregate_id character varying NOT NULL,
    event_id integer NOT NULL,
    snapshot integer,
    event json NOT NULL
);


ALTER TABLE public.event_store OWNER TO dbmasteruser;

--
-- TOC entry 242 (class 1259 OID 16749)
-- Name: filters_projection; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.filters_projection (
    aggregate_id character varying NOT NULL,
    event_id integer NOT NULL,
    rules json NOT NULL
);


ALTER TABLE public.filters_projection OWNER TO dbmasteruser;

--
-- TOC entry 232 (class 1259 OID 16676)
-- Name: mail_repository_content; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.mail_repository_content (
    url character varying(255) NOT NULL,
    key character varying NOT NULL,
    state character varying NOT NULL,
    error character varying,
    header_blob_id character varying NOT NULL,
    body_blob_id character varying NOT NULL,
    attributes public.hstore NOT NULL,
    sender character varying,
    recipients character varying[] NOT NULL,
    remote_host character varying NOT NULL,
    remote_address character varying NOT NULL,
    last_updated timestamp(6) without time zone NOT NULL,
    per_recipient_specific_headers public.hstore NOT NULL
);


ALTER TABLE public.mail_repository_content OWNER TO dbmasteruser;

--
-- TOC entry 231 (class 1259 OID 16671)
-- Name: mail_repository_url; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.mail_repository_url (
    url character varying(255) NOT NULL
);


ALTER TABLE public.mail_repository_url OWNER TO dbmasteruser;

--
-- TOC entry 219 (class 1259 OID 16574)
-- Name: mailbox; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.mailbox (
    mailbox_id uuid NOT NULL,
    mailbox_name character varying(255) NOT NULL,
    mailbox_uid_validity bigint NOT NULL,
    user_name character varying(255),
    mailbox_namespace character varying(255) NOT NULL,
    mailbox_last_uid bigint,
    mailbox_highest_modseq bigint,
    mailbox_acl public.hstore,
    mailbox_acl_version bigint DEFAULT 0 NOT NULL
);


ALTER TABLE public.mailbox OWNER TO dbmasteruser;

--
-- TOC entry 223 (class 1259 OID 16611)
-- Name: mailbox_annotations; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.mailbox_annotations (
    mailbox_id uuid NOT NULL,
    annotations public.hstore NOT NULL
);


ALTER TABLE public.mailbox_annotations OWNER TO dbmasteruser;

--
-- TOC entry 240 (class 1259 OID 16735)
-- Name: mailbox_change; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.mailbox_change (
    account_id character varying NOT NULL,
    state uuid NOT NULL,
    date timestamp with time zone NOT NULL,
    is_shared boolean NOT NULL,
    is_count_change boolean NOT NULL,
    created uuid[] NOT NULL,
    updated uuid[] NOT NULL,
    destroyed uuid[] NOT NULL
);


ALTER TABLE public.mailbox_change OWNER TO dbmasteruser;

--
-- TOC entry 221 (class 1259 OID 16591)
-- Name: message; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.message (
    message_id uuid NOT NULL,
    body_blob_id character varying(200) NOT NULL,
    mime_type character varying(200),
    mime_subtype character varying(200),
    internal_date timestamp(6) without time zone,
    size bigint NOT NULL,
    body_start_octet integer NOT NULL,
    header_content bytea NOT NULL,
    textual_line_count integer,
    content_description character varying(200),
    content_location character varying(200),
    content_transfer_encoding character varying(200),
    content_disposition_type character varying(200),
    content_id character varying(200),
    content_md5 character varying(200),
    content_language character varying[],
    content_type_parameters public.hstore,
    content_disposition_parameters public.hstore,
    attachment_metadata jsonb
);


ALTER TABLE public.message OWNER TO dbmasteruser;

--
-- TOC entry 4624 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE message; Type: COMMENT; Schema: public; Owner: dbmasteruser
--

COMMENT ON TABLE public.message IS 'Holds the metadata of a mail';


--
-- TOC entry 238 (class 1259 OID 16721)
-- Name: message_fast_view_projection; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.message_fast_view_projection (
    messageid uuid NOT NULL,
    preview character varying NOT NULL,
    has_attachment boolean NOT NULL
);


ALTER TABLE public.message_fast_view_projection OWNER TO dbmasteruser;

--
-- TOC entry 4625 (class 0 OID 0)
-- Dependencies: 238
-- Name: TABLE message_fast_view_projection; Type: COMMENT; Schema: public; Owner: dbmasteruser
--

COMMENT ON TABLE public.message_fast_view_projection IS 'Storing the JMAP projections for MessageFastView, an aggregation of JMAP properties expected to be fast to fetch.';


--
-- TOC entry 222 (class 1259 OID 16598)
-- Name: message_mailbox; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.message_mailbox (
    mailbox_id uuid NOT NULL,
    message_uid bigint NOT NULL,
    mod_seq bigint NOT NULL,
    message_id uuid NOT NULL,
    thread_id uuid,
    internal_date timestamp(6) without time zone,
    size bigint NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    is_answered boolean NOT NULL,
    is_draft boolean NOT NULL,
    is_flagged boolean NOT NULL,
    is_recent boolean NOT NULL,
    is_seen boolean NOT NULL,
    user_flags character varying[],
    save_date timestamp(6) without time zone
);


ALTER TABLE public.message_mailbox OWNER TO dbmasteruser;

--
-- TOC entry 4626 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE message_mailbox; Type: COMMENT; Schema: public; Owner: dbmasteruser
--

COMMENT ON TABLE public.message_mailbox IS 'Holds mailbox and flags for each message';


--
-- TOC entry 241 (class 1259 OID 16742)
-- Name: push_subscription; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.push_subscription (
    username character varying NOT NULL,
    device_client_id character varying NOT NULL,
    id uuid NOT NULL,
    expires timestamp(6) with time zone,
    types character varying[] NOT NULL,
    url character varying NOT NULL,
    verification_code character varying,
    encrypt_public_key character varying,
    encrypt_auth_secret character varying,
    validated boolean NOT NULL
);


ALTER TABLE public.push_subscription OWNER TO dbmasteruser;

--
-- TOC entry 226 (class 1259 OID 16636)
-- Name: quota_current_value; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.quota_current_value (
    identifier character varying NOT NULL,
    component character varying NOT NULL,
    type character varying NOT NULL,
    current_value bigint NOT NULL
);


ALTER TABLE public.quota_current_value OWNER TO dbmasteruser;

--
-- TOC entry 227 (class 1259 OID 16643)
-- Name: quota_limit; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.quota_limit (
    quota_scope character varying NOT NULL,
    identifier character varying NOT NULL,
    quota_component character varying NOT NULL,
    quota_type character varying NOT NULL,
    quota_limit bigint
);


ALTER TABLE public.quota_limit OWNER TO dbmasteruser;

--
-- TOC entry 230 (class 1259 OID 16664)
-- Name: rrt; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.rrt (
    username character varying(255) NOT NULL,
    domain_name character varying(255) NOT NULL,
    target_address character varying(255) NOT NULL
);


ALTER TABLE public.rrt OWNER TO dbmasteruser;

--
-- TOC entry 233 (class 1259 OID 16683)
-- Name: sieve_scripts; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.sieve_scripts (
    script_id uuid NOT NULL,
    username character varying(255) NOT NULL,
    script_name character varying NOT NULL,
    script_size bigint NOT NULL,
    script_content character varying NOT NULL,
    is_active boolean NOT NULL,
    activation_date_time timestamp with time zone
);


ALTER TABLE public.sieve_scripts OWNER TO dbmasteruser;

--
-- TOC entry 220 (class 1259 OID 16584)
-- Name: subscription; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.subscription (
    mailbox character varying(255) NOT NULL,
    user_name character varying(255) NOT NULL
);


ALTER TABLE public.subscription OWNER TO dbmasteruser;

--
-- TOC entry 218 (class 1259 OID 16567)
-- Name: task_execution_details_projection; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.task_execution_details_projection (
    task_id uuid NOT NULL,
    additional_information jsonb,
    type character varying,
    status character varying,
    submitted_date timestamp(6) without time zone,
    submitted_node character varying,
    started_date timestamp(6) without time zone,
    ran_node character varying,
    completed_date timestamp(6) without time zone,
    canceled_date timestamp(6) without time zone,
    cancel_requested_node character varying,
    failed_date timestamp(6) without time zone
);


ALTER TABLE public.task_execution_details_projection OWNER TO dbmasteruser;

--
-- TOC entry 225 (class 1259 OID 16631)
-- Name: thread; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.thread (
    username character varying(255) NOT NULL,
    hash_mime_message_id integer NOT NULL,
    message_id uuid NOT NULL,
    thread_id uuid NOT NULL,
    hash_base_subject integer
);


ALTER TABLE public.thread OWNER TO dbmasteruser;

--
-- TOC entry 237 (class 1259 OID 16714)
-- Name: uploads; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.uploads (
    id uuid NOT NULL,
    content_type character varying,
    size bigint NOT NULL,
    blob_id character varying NOT NULL,
    user_name character varying NOT NULL,
    upload_date timestamp(6) without time zone NOT NULL
);


ALTER TABLE public.uploads OWNER TO dbmasteruser;

--
-- TOC entry 245 (class 1259 OID 16768)
-- Name: users; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.users (
    username character varying(255) NOT NULL,
    hashed_password character varying,
    algorithm character varying(100),
    authorized_users character varying[],
    delegated_users character varying[]
);


ALTER TABLE public.users OWNER TO dbmasteruser;

--
-- TOC entry 236 (class 1259 OID 16707)
-- Name: vacation_notification_registry; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.vacation_notification_registry (
    account_id character varying NOT NULL,
    recipient_id character varying NOT NULL,
    expiry_date timestamp(6) without time zone
);


ALTER TABLE public.vacation_notification_registry OWNER TO dbmasteruser;

--
-- TOC entry 235 (class 1259 OID 16699)
-- Name: vacation_response; Type: TABLE; Schema: public; Owner: dbmasteruser
--

CREATE TABLE public.vacation_response (
    account_id character varying NOT NULL,
    is_enabled boolean DEFAULT false NOT NULL,
    from_date timestamp(6) without time zone,
    to_date timestamp(6) without time zone,
    text character varying,
    subject character varying,
    html character varying
);


ALTER TABLE public.vacation_response OWNER TO dbmasteruser;

--
-- TOC entry 4596 (class 0 OID 16623)
-- Dependencies: 224
-- Data for Name: attachment; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.attachment (id, blob_id, type, message_id, size) FROM stdin;
\.


--
-- TOC entry 4615 (class 0 OID 16756)
-- Dependencies: 243
-- Data for Name: custom_identity; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.custom_identity (username, id, name, email, reply_to, bcc, text_signature, html_signature, sort_order, may_delete) FROM stdin;
\.


--
-- TOC entry 4601 (class 0 OID 16657)
-- Dependencies: 229
-- Data for Name: domains; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.domains (domain) FROM stdin;
localhost
2gvp.com
2gvp.net
2ndgenfilms.com
2ndgenmedia.com
fmancuso.com
forkliftfred.com
forklifts.com
jwmhosting.com
malcolmbusinesssolutions.com
malcolmenterprises.com
malcolms.com
2ndgenerationproductions.com
\.


--
-- TOC entry 4611 (class 0 OID 16728)
-- Dependencies: 239
-- Data for Name: email_change; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.email_change (account_id, state, date, is_shared, created, updated, destroyed) FROM stdin;
\.


--
-- TOC entry 4616 (class 0 OID 16763)
-- Dependencies: 244
-- Data for Name: email_query_view; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.email_query_view (mailbox_id, message_id, received_at, sent_at) FROM stdin;
\.


--
-- TOC entry 4600 (class 0 OID 16650)
-- Dependencies: 228
-- Data for Name: event_dead_letters; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.event_dead_letters (insertion_id, "group", event) FROM stdin;
\.


--
-- TOC entry 4606 (class 0 OID 16692)
-- Dependencies: 234
-- Data for Name: event_store; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.event_store (aggregate_id, event_id, snapshot, event) FROM stdin;
BlobStoreStorageStrategyConfiguration	0	\N	{"eventId":0,"aggregateKey":"BlobStoreStorageStrategyConfiguration","type":"storage-strategy-changed","storageStrategy":"PASSTHROUGH"}
\.


--
-- TOC entry 4614 (class 0 OID 16749)
-- Dependencies: 242
-- Data for Name: filters_projection; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.filters_projection (aggregate_id, event_id, rules) FROM stdin;
\.


--
-- TOC entry 4604 (class 0 OID 16676)
-- Dependencies: 232
-- Data for Name: mail_repository_content; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.mail_repository_content (url, key, state, error, header_blob_id, body_blob_id, attributes, sender, recipients, remote_host, remote_address, last_updated, per_recipient_specific_headers) FROM stdin;
\.


--
-- TOC entry 4603 (class 0 OID 16671)
-- Dependencies: 231
-- Data for Name: mail_repository_url; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.mail_repository_url (url) FROM stdin;
postgres://var/mail/error
postgres://var/mail/relay-limit-exceeded
postgres://var/mail/address-error
postgres://var/mail/relay-denied
postgres://var/mail/rrt-error
\.


--
-- TOC entry 4591 (class 0 OID 16574)
-- Dependencies: 219
-- Data for Name: mailbox; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.mailbox (mailbox_id, mailbox_name, mailbox_uid_validity, user_name, mailbox_namespace, mailbox_last_uid, mailbox_highest_modseq, mailbox_acl, mailbox_acl_version) FROM stdin;
\.


--
-- TOC entry 4595 (class 0 OID 16611)
-- Dependencies: 223
-- Data for Name: mailbox_annotations; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.mailbox_annotations (mailbox_id, annotations) FROM stdin;
\.


--
-- TOC entry 4612 (class 0 OID 16735)
-- Dependencies: 240
-- Data for Name: mailbox_change; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.mailbox_change (account_id, state, date, is_shared, is_count_change, created, updated, destroyed) FROM stdin;
\.


--
-- TOC entry 4593 (class 0 OID 16591)
-- Dependencies: 221
-- Data for Name: message; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.message (message_id, body_blob_id, mime_type, mime_subtype, internal_date, size, body_start_octet, header_content, textual_line_count, content_description, content_location, content_transfer_encoding, content_disposition_type, content_id, content_md5, content_language, content_type_parameters, content_disposition_parameters, attachment_metadata) FROM stdin;
\.


--
-- TOC entry 4610 (class 0 OID 16721)
-- Dependencies: 238
-- Data for Name: message_fast_view_projection; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.message_fast_view_projection (messageid, preview, has_attachment) FROM stdin;
\.


--
-- TOC entry 4594 (class 0 OID 16598)
-- Dependencies: 222
-- Data for Name: message_mailbox; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.message_mailbox (mailbox_id, message_uid, mod_seq, message_id, thread_id, internal_date, size, is_deleted, is_answered, is_draft, is_flagged, is_recent, is_seen, user_flags, save_date) FROM stdin;
\.


--
-- TOC entry 4613 (class 0 OID 16742)
-- Dependencies: 241
-- Data for Name: push_subscription; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.push_subscription (username, device_client_id, id, expires, types, url, verification_code, encrypt_public_key, encrypt_auth_secret, validated) FROM stdin;
\.


--
-- TOC entry 4598 (class 0 OID 16636)
-- Dependencies: 226
-- Data for Name: quota_current_value; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.quota_current_value (identifier, component, type, current_value) FROM stdin;
\.


--
-- TOC entry 4599 (class 0 OID 16643)
-- Dependencies: 227
-- Data for Name: quota_limit; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.quota_limit (quota_scope, identifier, quota_component, quota_type, quota_limit) FROM stdin;
\.


--
-- TOC entry 4602 (class 0 OID 16664)
-- Dependencies: 230
-- Data for Name: rrt; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.rrt (username, domain_name, target_address) FROM stdin;
pbm2	malcolms.com	alias:penney@malcolms.com
fred	forklifts.com	alias:fred95@forklifts.com
info	jwmhosting.com	alias:jerry@jwmhosting.com
abuse	forklifts.com	alias:jerry@jwmhosting.com
billing	jwmhosting.com	alias:jerry@jwmhosting.com
abuse	2gvp.com	alias:jerry@jwmhosting.com
marketing	forkliftfred.com	alias:fred@forkliftfred.com
jobs	malcolmbusinesssolutions.com	alias:Jerry@malcolmbusinesssolutions.com
penny	malcolms.com	alias:penney@malcolms.com
postmaster	2gvp.net	alias:jerry@jwmhosting.com
penny	2gvp.com	alias:2gvp-mail@2gvp.net
parts	forkliftfred.com	alias:fred@forkliftfred.com
meredith	2gvp.com	alias:2gvp-mail@2gvp.net
shopper	fmancuso.com	alias:fred@fmancuso.com
jerry	2ndgenfilms.com	alias:2gvp-mail@2gvp.net
postmaster	jwmhosting.com	alias:jerry@jwmhosting.com
customerservice	forkliftfred.com	alias:fred@forkliftfred.com
abuse	2ndgenerationproductions.com	alias:jerry@jwmhosting.com
postmaster	2ndgenfilms.com	alias:jerry@jwmhosting.com
info	malcolmbusinesssolutions.com	alias:Jerry@malcolmbusinesssolutions.com
postmaster	2ndgenerationproductions.com	alias:jerry@jwmhosting.com
abuse	malcolms.com	alias:jerry@jwmhosting.com
postmaster	fmancuso.com	alias:jerry@jwmhosting.com
abuse	fmancuso.com	alias:jerry@jwmhosting.com
jerry	2gvp.com	alias:2gvp-mail@2gvp.net
fmancuso	forkliftfred.com	alias:fred@forkliftfred.com
abuse	malcolmenterprises.com	alias:jerry@jwmhosting.com
penney	2gvp.com	alias:2gvp-mail@2gvp.net
abuse	forkliftfred.com	alias:jerry@jwmhosting.com
info	2ndgenfilms.com	alias:2gvp-mail@2gvp.net
photo	2ndgenmedia.com	alias:2gvp-mail@2gvp.net
abuse	2gvp.net	alias:jerry@jwmhosting.com
fabrizio	forkliftfred.com	alias:fred@forkliftfred.com
webmaster	jwmhosting.com	alias:jerry@jwmhosting.com
vendor	forkliftfred.com	alias:fred@forkliftfred.com
postmaster	malcolmenterprises.com	alias:jerry@jwmhosting.com
postmaster	forkliftfred.com	alias:jerry@jwmhosting.com
webmaster	malcolms.com	alias:jerry@malcolms.com
jerry	2ndgenerationproductions.com	alias:2gvp-mail@2gvp.net
abuse	2ndgenfilms.com	alias:jerry@jwmhosting.com
postmaster	2gvp.com	alias:jerry@jwmhosting.com
hostmaster	jwmhosting.com	alias:jerry@jwmhosting.com
admin	forkliftfred.com	alias:fred@forkliftfred.com
mailhandler	jwmhosting.com	alias:jerry@jwmhosting.com
fabrizio	fmancuso.com	alias:fred@fmancuso.com
accounting	forklifts.com	alias:fred95@forklifts.com
webmaster	2gvp.com	alias:2gvp-mail@2gvp.net
abuse	jwmhosting.com	alias:jerry@jwmhosting.com
traffic	forkliftfred.com	alias:fred@forkliftfred.com
postmaster	malcolms.com	alias:jerry@jwmhosting.com
info	2gvp.com	alias:2gvp-mail@2gvp.net
tldr	malcolmbusinesssolutions.com	alias:jerry@malcolmbusinesssolutions.com
abuse	localhost	alias:jerry@jwmhosting.com
2gvpbatch	2gvp.com	alias:2gvpbatch@2ndgenerationproductions.com
fabrizio	forklifts.com	alias:fred95@forklifts.com
registry	2gvp.com	alias:2gvp-mail@2gvp.net
support	forkliftfred.com	alias:fred@forkliftfred.com
pbm1	malcolms.com	alias:penney@malcolms.com
postmaster	localhost	alias:jerry@jwmhosting.com
warranty	forkliftfred.com	alias:fred@forkliftfred.com
video	2ndgenmedia.com	alias:2gvp-mail@2gvp.net
accounting	forkliftfred.com	alias:nadine@forkliftfred.com
fmancuso	forklifts.com	alias:fred95@forklifts.com
leah	2gvp.com	alias:2gvp-mail@2gvp.net
bridalregistry	2gvp.com	alias:2gvp-mail@2gvp.net
2gvpbatchtest	2gvp.com	alias:2gvpbatchtest@2ndgenerationproductions.com
info	2ndgenerationproductions.com	alias:2gvp-mail@2gvp.net
webmaster	2ndgenerationproductions.com	alias:2gvp-mail@2gvp.net
\.


--
-- TOC entry 4605 (class 0 OID 16683)
-- Dependencies: 233
-- Data for Name: sieve_scripts; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.sieve_scripts (script_id, username, script_name, script_size, script_content, is_active, activation_date_time) FROM stdin;
\.


--
-- TOC entry 4592 (class 0 OID 16584)
-- Dependencies: 220
-- Data for Name: subscription; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.subscription (mailbox, user_name) FROM stdin;
\.


--
-- TOC entry 4590 (class 0 OID 16567)
-- Dependencies: 218
-- Data for Name: task_execution_details_projection; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.task_execution_details_projection (task_id, additional_information, type, status, submitted_date, submitted_node, started_date, ran_node, completed_date, canceled_date, cancel_requested_node, failed_date) FROM stdin;
\.


--
-- TOC entry 4597 (class 0 OID 16631)
-- Dependencies: 225
-- Data for Name: thread; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.thread (username, hash_mime_message_id, message_id, thread_id, hash_base_subject) FROM stdin;
\.


--
-- TOC entry 4609 (class 0 OID 16714)
-- Dependencies: 237
-- Data for Name: uploads; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.uploads (id, content_type, size, blob_id, user_name, upload_date) FROM stdin;
\.


--
-- TOC entry 4617 (class 0 OID 16768)
-- Dependencies: 245
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.users (username, hashed_password, algorithm, authorized_users, delegated_users) FROM stdin;
2gvp-mail@2gvp.net	Mx+p2NCq2aNTSfIFhG9b5cDN7YUn8jxiyCMtL0dyJONwggZI8k3db7R1VXWdIsp3tDQ0U3gteusK\r\n7ctw9BAqnQ==\r\n	PBKDF2-SHA512/plain	\N	\N
batch@2gvp.net	T3dKFmAeRKrcpORFjWikJBphu8EGl4CePXgqEXR7WyKOlNVqNgAad5Uo4BtaxxgxuhONG0L1ugNG\r\nYosZhAqkNQ==\r\n	PBKDF2-SHA512/plain	\N	\N
developer@forklifts.com	nC2zrD493OGHvhra4+fG+FMYMSOkQXuh/2GcgzXSr7+nKm5n+//clmysmO7sm7QGUPplwdf9jCeI\r\nn3VrLKghXQ==\r\n	PBKDF2-SHA512/plain	\N	\N
fred95@forklifts.com	+TA6ckzbSwyZLczrn/3ZAyx5MrFr1BmbvousRsmZ/Y2hL1irT7L9XS8COP7TNobGsQ0xFMg/flWv\r\nnoawZlu9BQ==\r\n	PBKDF2-SHA512/plain	\N	\N
fred@fmancuso.com	Tw8KmmAc6N/ZcybkMmAXjTUMm9AaTkmqntFnFEzIZwG8jvGkeKQc2EqTQHQ6OuDVxkkAhL1BADif\r\nK/0+cx5r6A==\r\n	PBKDF2-SHA512/plain	\N	\N
fred@forkliftfred.com	DS2BxFNSJbJUCFtDrA+8dR8cOuOOCNKacC5UHR+Th1ZwbouL/thnF1FQ2HmgM1NKjK1cSlQ6anjA\r\ntw/n3v9uIA==\r\n	PBKDF2-SHA512/plain	\N	\N
jerry@forklifts.com	cbd3cnA1j65XPB2DVSRagE46AoxY5Tmbv6tFB66PIS7l2B+iQUtqcuBhd4CDKGi0G5ifYAR6p2XK\r\nxTbTzmelHA==\r\n	PBKDF2-SHA512/plain	\N	\N
jerry@jwmhosting.com	pQpX/MSpDZjiNE2cpiYmo1QxXkiO/xWPMo/XtDB0Ye05utqOHQ4XzOyG9p7ba+tMZmG4x1KcKoAr\r\nx2iDyJO6gw==\r\n	PBKDF2-SHA512/plain	\N	\N
jerry@malcolmbusinesssolutions.com	7kf2/DXNRb5LOtDfg5059mOuZyXWUoMV5sI8LHLM8HQh3lNkhUoMONs+2GNCHIhi25NtpbMLnK56\r\nfrs9lH40PQ==\r\n	PBKDF2-SHA512/plain	\N	\N
jerry@malcolmenterprises.com	aTbDF0Wc74zqKYk9n3PT19b5aoiseF4hkk4WAZYfwjCbAc560bdT1HB34J+iTYublRsH/wXVKX6X\r\nqqa3tg4NVA==\r\n	PBKDF2-SHA512/plain	\N	\N
jerry@malcolms.com	cJzQihYrhrJiLrZoj4bKu6r6pPosxqXYr+ESfu8k4F/okxyekRsPHlUPaID9tIXyYNPtrDzAa7zj\r\nBKhyAjo6Jw==\r\n	PBKDF2-SHA512/plain	\N	\N
jmalcolm@malcolms.com	ZquHHuLFzzFahdJXi9ZL23sUdOLjxkHSSnAp8O2zQlVccqEH1vHXFIGzV1nxoCzz9IsL9Fkxd3Fq\r\nGLoLJ8tjrA==\r\n	PBKDF2-SHA512/plain	\N	\N
jwm1@malcolms.com	GL7pu5BoKIESusRKgT4+BJPbFPbfsOF3/8+H7dBO52/O1PrFyx/KfECDc49Uh31+s9ctqXXU5r4c\r\nYPEeafPs4A==\r\n	PBKDF2-SHA512/plain	\N	\N
jwm2@malcolms.com	8GZ3Ts0ikfQW+iQslcXsXOpNgI0+Kwk6baI9NvOBbZtuQkEVBkJzu8HNC8g3RdEoQn8AEKKQoL9U\r\ndrRA2cSteA==\r\n	PBKDF2-SHA512/plain	\N	\N
kristaxxxxxx@forklifts.com	49n929ghFXAMZnpDLOtdixqZviX9J34fqe6V4f4FUp/bTdCA/gHaMw+eGZnmPVg6/BqC2Cq2WOhV\r\nuadY3vLjAA==\r\n	PBKDF2-SHA512/plain	\N	\N
localfolders@malcolms.com	eI34SGGgY7g/r/QCVkFcGR5MyaBsY5oSdCfJ0ihK3znVWkcnzYeaWxjWhyP6ipkgUeoLYXBsDFYJ\r\noU3TGj1tmA==\r\n	PBKDF2-SHA512/plain	\N	\N
logger@jwmhosting.com	SU5wP4hiThgOD9Z5S5WUo+rtqloLABg6/GD7ACiZA0U5RyFQJbGmgt1+8Rv/8zv1u08AMSd1ww2s\r\nbmPtxXUKMQ==\r\n	PBKDF2-SHA512/plain	\N	\N
nadine@forkliftfred.com	vJZPBxn/iTpX1isEx24PSJwjzfK8EZWBW4uBiBGRh/ZDU9nLggcK9LlWACMYm8YOFBxxm9CR4Rz4\r\nJVAikEG3gQ==\r\n	PBKDF2-SHA512/plain	\N	\N
nadinexxxxx@forklifts.com	B+QIN0tMwisrE35jkfu5YT3NhzzU1pC63TdGNY/i15tOGWP/j9QKA7VY5Pn5oL3eOqGORDpR/pqr\r\nD/sCkCJC8g==\r\n	PBKDF2-SHA512/plain	\N	\N
penney@malcolms.com	8xSPZLXc68DL+T9041QGF36xVdlis4Kssyh1gIm0bMvQosPnHoVlBHuctsfAMoDlTw1wa3D7b3c/\r\ng0dpj1mTKg==\r\n	PBKDF2-SHA512/plain	\N	\N
rv@malcolms.com	TJxusCnvLznfFGy+3XPQN4J15s1QryF3noY9OyMy/JxER3ndZ6G++py1itiFJY/2FOFp1wzLk78o\r\n+Ynkz71wUw==\r\n	PBKDF2-SHA512/plain	\N	\N
techstuff@malcolms.com	U8bz0KZ6DnV+sS4eA8dfF03A6NksvUJXYPABWUyjDWhdkJncGiGGFiAI5Un80Z87FK2FqIkjMCFc\r\nZ3uXKll+uw==\r\n	PBKDF2-SHA512/plain	\N	\N
webmaster@jwmhosting.com	Akff4/kftTojRrg7PHQ/1RDHefVqBlXzX6bc53hYLfw5t581TqkRsnut1r9HnXguIA0X+TZurGQJ\r\n8pOSbKEcbw==\r\n	PBKDF2-SHA512/plain	\N	\N
\.


--
-- TOC entry 4608 (class 0 OID 16707)
-- Dependencies: 236
-- Data for Name: vacation_notification_registry; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.vacation_notification_registry (account_id, recipient_id, expiry_date) FROM stdin;
\.


--
-- TOC entry 4607 (class 0 OID 16699)
-- Dependencies: 235
-- Data for Name: vacation_response; Type: TABLE DATA; Schema: public; Owner: dbmasteruser
--

COPY public.vacation_response (account_id, is_enabled, from_date, to_date, text, subject, html) FROM stdin;
\.


--
-- TOC entry 4379 (class 2606 OID 16629)
-- Name: attachment attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.attachment
    ADD CONSTRAINT attachment_pkey PRIMARY KEY (id);


--
-- TOC entry 4434 (class 2606 OID 16762)
-- Name: custom_identity custom_identity_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.custom_identity
    ADD CONSTRAINT custom_identity_pkey PRIMARY KEY (username, id);


--
-- TOC entry 4392 (class 2606 OID 16663)
-- Name: domains domains_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.domains
    ADD CONSTRAINT domains_pkey PRIMARY KEY (domain);


--
-- TOC entry 4422 (class 2606 OID 16734)
-- Name: email_change email_change_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.email_change
    ADD CONSTRAINT email_change_pkey PRIMARY KEY (account_id, state, is_shared);


--
-- TOC entry 4440 (class 2606 OID 16767)
-- Name: email_query_view email_query_view_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.email_query_view
    ADD CONSTRAINT email_query_view_pkey PRIMARY KEY (mailbox_id, message_id);


--
-- TOC entry 4390 (class 2606 OID 16656)
-- Name: event_dead_letters event_dead_letters_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.event_dead_letters
    ADD CONSTRAINT event_dead_letters_pkey PRIMARY KEY (insertion_id);


--
-- TOC entry 4407 (class 2606 OID 16698)
-- Name: event_store event_store_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.event_store
    ADD CONSTRAINT event_store_pkey PRIMARY KEY (aggregate_id, event_id);


--
-- TOC entry 4432 (class 2606 OID 16755)
-- Name: filters_projection filters_projection_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.filters_projection
    ADD CONSTRAINT filters_projection_pkey PRIMARY KEY (aggregate_id);


--
-- TOC entry 4399 (class 2606 OID 16682)
-- Name: mail_repository_content mail_repository_content_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mail_repository_content
    ADD CONSTRAINT mail_repository_content_pkey PRIMARY KEY (url, key);


--
-- TOC entry 4397 (class 2606 OID 16675)
-- Name: mail_repository_url mail_repository_url_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mail_repository_url
    ADD CONSTRAINT mail_repository_url_pkey PRIMARY KEY (url);


--
-- TOC entry 4376 (class 2606 OID 16617)
-- Name: mailbox_annotations mailbox_annotations_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mailbox_annotations
    ADD CONSTRAINT mailbox_annotations_pkey PRIMARY KEY (mailbox_id);


--
-- TOC entry 4426 (class 2606 OID 16741)
-- Name: mailbox_change mailbox_change_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mailbox_change
    ADD CONSTRAINT mailbox_change_pkey PRIMARY KEY (account_id, state, is_shared);


--
-- TOC entry 4359 (class 2606 OID 16583)
-- Name: mailbox mailbox_mailbox_name_user_name_mailbox_namespace_key; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mailbox
    ADD CONSTRAINT mailbox_mailbox_name_user_name_mailbox_namespace_key UNIQUE (mailbox_name, user_name, mailbox_namespace);


--
-- TOC entry 4361 (class 2606 OID 16581)
-- Name: mailbox mailbox_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mailbox
    ADD CONSTRAINT mailbox_pkey PRIMARY KEY (mailbox_id);


--
-- TOC entry 4420 (class 2606 OID 16727)
-- Name: message_fast_view_projection message_fast_view_projection_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.message_fast_view_projection
    ADD CONSTRAINT message_fast_view_projection_pkey PRIMARY KEY (messageid);


--
-- TOC entry 4374 (class 2606 OID 16605)
-- Name: message_mailbox message_mailbox_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.message_mailbox
    ADD CONSTRAINT message_mailbox_pkey PRIMARY KEY (mailbox_id, message_uid);


--
-- TOC entry 4367 (class 2606 OID 16597)
-- Name: message message_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.message
    ADD CONSTRAINT message_pkey PRIMARY KEY (message_id);


--
-- TOC entry 4428 (class 2606 OID 16748)
-- Name: push_subscription push_subscription_primary_key_constraint; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.push_subscription
    ADD CONSTRAINT push_subscription_primary_key_constraint PRIMARY KEY (username, device_client_id);


--
-- TOC entry 4385 (class 2606 OID 16642)
-- Name: quota_current_value quota_current_value_primary_key; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.quota_current_value
    ADD CONSTRAINT quota_current_value_primary_key PRIMARY KEY (identifier, component, type);


--
-- TOC entry 4387 (class 2606 OID 16649)
-- Name: quota_limit quota_limit_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.quota_limit
    ADD CONSTRAINT quota_limit_pkey PRIMARY KEY (quota_scope, identifier, quota_component, quota_type);


--
-- TOC entry 4395 (class 2606 OID 16670)
-- Name: rrt rrt_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.rrt
    ADD CONSTRAINT rrt_pkey PRIMARY KEY (username, domain_name, target_address);


--
-- TOC entry 4402 (class 2606 OID 16689)
-- Name: sieve_scripts sieve_scripts_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.sieve_scripts
    ADD CONSTRAINT sieve_scripts_pkey PRIMARY KEY (script_id);


--
-- TOC entry 4404 (class 2606 OID 16691)
-- Name: sieve_scripts sieve_scripts_username_script_name_key; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.sieve_scripts
    ADD CONSTRAINT sieve_scripts_username_script_name_key UNIQUE (username, script_name);


--
-- TOC entry 4364 (class 2606 OID 16590)
-- Name: subscription subscription_mailbox_user_name_key; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.subscription
    ADD CONSTRAINT subscription_mailbox_user_name_key UNIQUE (mailbox, user_name);


--
-- TOC entry 4355 (class 2606 OID 16573)
-- Name: task_execution_details_projection task_execution_details_projection_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.task_execution_details_projection
    ADD CONSTRAINT task_execution_details_projection_pkey PRIMARY KEY (task_id);


--
-- TOC entry 4382 (class 2606 OID 16635)
-- Name: thread thread_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.thread
    ADD CONSTRAINT thread_pkey PRIMARY KEY (username, hash_mime_message_id, message_id);


--
-- TOC entry 4416 (class 2606 OID 16720)
-- Name: uploads uploads_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.uploads
    ADD CONSTRAINT uploads_pkey PRIMARY KEY (id);


--
-- TOC entry 4442 (class 2606 OID 16774)
-- Name: users users_username_pk; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_pk PRIMARY KEY (username);


--
-- TOC entry 4412 (class 2606 OID 16713)
-- Name: vacation_notification_registry vacation_notification_registry_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.vacation_notification_registry
    ADD CONSTRAINT vacation_notification_registry_pkey PRIMARY KEY (account_id, recipient_id);


--
-- TOC entry 4409 (class 2606 OID 16706)
-- Name: vacation_response vacation_response_pkey; Type: CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.vacation_response
    ADD CONSTRAINT vacation_response_pkey PRIMARY KEY (account_id);


--
-- TOC entry 4377 (class 1259 OID 16784)
-- Name: attachment_message_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX attachment_message_id_index ON public.attachment USING btree (message_id);


--
-- TOC entry 4435 (class 1259 OID 16800)
-- Name: custom_identity_username_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX custom_identity_username_index ON public.custom_identity USING btree (username);


--
-- TOC entry 4436 (class 1259 OID 16802)
-- Name: email_query_view_mailbox_id__received_at_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX email_query_view_mailbox_id__received_at_index ON public.email_query_view USING btree (mailbox_id, received_at);


--
-- TOC entry 4437 (class 1259 OID 16801)
-- Name: email_query_view_mailbox_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX email_query_view_mailbox_id_index ON public.email_query_view USING btree (mailbox_id);


--
-- TOC entry 4438 (class 1259 OID 16803)
-- Name: email_query_view_mailbox_id_sent_at_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX email_query_view_mailbox_id_sent_at_index ON public.email_query_view USING btree (mailbox_id, sent_at);


--
-- TOC entry 4388 (class 1259 OID 16787)
-- Name: event_dead_letters_group_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX event_dead_letters_group_index ON public.event_dead_letters USING btree ("group");


--
-- TOC entry 4405 (class 1259 OID 16790)
-- Name: event_store_aggregate_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX event_store_aggregate_id_index ON public.event_store USING btree (aggregate_id);


--
-- TOC entry 4423 (class 1259 OID 16796)
-- Name: idx_email_change_date; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX idx_email_change_date ON public.email_change USING btree (date);


--
-- TOC entry 4393 (class 1259 OID 16788)
-- Name: idx_rrt_target_address; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX idx_rrt_target_address ON public.rrt USING btree (target_address);


--
-- TOC entry 4424 (class 1259 OID 16797)
-- Name: index_mailbox_change_date; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX index_mailbox_change_date ON public.mailbox_change USING btree (date);


--
-- TOC entry 4368 (class 1259 OID 16783)
-- Name: mailbox_id_is_delete_mail_uid_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_id_is_delete_mail_uid_index ON public.message_mailbox USING btree (mailbox_id, is_deleted, message_uid);


--
-- TOC entry 4369 (class 1259 OID 16782)
-- Name: mailbox_id_is_recent_mail_uid_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_id_is_recent_mail_uid_index ON public.message_mailbox USING btree (mailbox_id, is_recent, message_uid);


--
-- TOC entry 4370 (class 1259 OID 16781)
-- Name: mailbox_id_is_seen_mail_uid_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_id_is_seen_mail_uid_index ON public.message_mailbox USING btree (mailbox_id, is_seen, message_uid);


--
-- TOC entry 4371 (class 1259 OID 16780)
-- Name: mailbox_id_mail_uid_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_id_mail_uid_index ON public.message_mailbox USING btree (mailbox_id, message_uid);


--
-- TOC entry 4357 (class 1259 OID 16630)
-- Name: mailbox_mailbox_acl_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_mailbox_acl_index ON public.mailbox USING gin (mailbox_acl);


--
-- TOC entry 4362 (class 1259 OID 16777)
-- Name: mailbox_username_namespace_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX mailbox_username_namespace_index ON public.mailbox USING btree (user_name, mailbox_namespace);


--
-- TOC entry 4400 (class 1259 OID 16789)
-- Name: maximum_one_active_script_per_user; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE UNIQUE INDEX maximum_one_active_script_per_user ON public.sieve_scripts USING btree (username) WHERE is_active;


--
-- TOC entry 4372 (class 1259 OID 16779)
-- Name: message_mailbox_message_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX message_mailbox_message_id_index ON public.message_mailbox USING btree (message_id);


--
-- TOC entry 4429 (class 1259 OID 16799)
-- Name: push_subscription_username_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX push_subscription_username_id_index ON public.push_subscription USING btree (username, id);


--
-- TOC entry 4430 (class 1259 OID 16798)
-- Name: push_subscription_username_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX push_subscription_username_index ON public.push_subscription USING btree (username);


--
-- TOC entry 4365 (class 1259 OID 16778)
-- Name: subscription_user_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX subscription_user_index ON public.subscription USING btree (user_name);


--
-- TOC entry 4356 (class 1259 OID 16776)
-- Name: task_execution_details_projection_submitteddate_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX task_execution_details_projection_submitteddate_index ON public.task_execution_details_projection USING btree (submitted_date);


--
-- TOC entry 4380 (class 1259 OID 16785)
-- Name: thread_message_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX thread_message_id_index ON public.thread USING btree (username, message_id);


--
-- TOC entry 4383 (class 1259 OID 16786)
-- Name: thread_thread_id_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX thread_thread_id_index ON public.thread USING btree (username, thread_id);


--
-- TOC entry 4414 (class 1259 OID 16794)
-- Name: uploads_id_user_name_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX uploads_id_user_name_index ON public.uploads USING btree (id, user_name);


--
-- TOC entry 4417 (class 1259 OID 16795)
-- Name: uploads_upload_date_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX uploads_upload_date_index ON public.uploads USING btree (upload_date);


--
-- TOC entry 4418 (class 1259 OID 16793)
-- Name: uploads_user_name_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX uploads_user_name_index ON public.uploads USING btree (user_name);


--
-- TOC entry 4410 (class 1259 OID 16791)
-- Name: vacation_notification_registry_accountid_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX vacation_notification_registry_accountid_index ON public.vacation_notification_registry USING btree (account_id);


--
-- TOC entry 4413 (class 1259 OID 16792)
-- Name: vnr_accountid_recipientid_expirydate_index; Type: INDEX; Schema: public; Owner: dbmasteruser
--

CREATE INDEX vnr_accountid_recipientid_expirydate_index ON public.vacation_notification_registry USING btree (account_id, recipient_id, expiry_date);


--
-- TOC entry 4444 (class 2606 OID 16618)
-- Name: mailbox_annotations mailbox_annotations_mailbox_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.mailbox_annotations
    ADD CONSTRAINT mailbox_annotations_mailbox_id_fkey FOREIGN KEY (mailbox_id) REFERENCES public.mailbox(mailbox_id) ON DELETE CASCADE;


--
-- TOC entry 4443 (class 2606 OID 16606)
-- Name: message_mailbox message_mailbox_message_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: dbmasteruser
--

ALTER TABLE ONLY public.message_mailbox
    ADD CONSTRAINT message_mailbox_message_id_fkey FOREIGN KEY (message_id) REFERENCES public.message(message_id);


-- Completed on 2026-01-03 21:37:35

--
-- PostgreSQL database dump complete
--

