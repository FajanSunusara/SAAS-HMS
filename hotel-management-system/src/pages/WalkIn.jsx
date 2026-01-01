import React, { useEffect, useMemo, useRef, useState } from "react";

/**
 * WalkInCheckIn.jsx (single file)
 * Enhanced with improved padding, margins, and spacing for a more elegant UI
 */

const Icon = ({ name, className = "" }) => (
  <span className={`material-symbols-outlined ${className}`}>{name}</span>
);

const uid = (prefix = "id") => `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
const money = (n) => (Number.isFinite(Number(n)) ? Number(n).toFixed(2) : "0.00");
const clamp = (n, min, max) => Math.min(max, Math.max(min, n));

const todayISO = () => new Date().toISOString().split("T")[0];
const addDaysISO = (days) => {
  const d = new Date();
  d.setDate(d.getDate() + days);
  return d.toISOString().split("T")[0];
};
const safeNights = (checkIn, checkOut) => {
  if (!checkIn || !checkOut) return 1;
  const inD = new Date(checkIn);
  const outD = new Date(checkOut);
  const ms = outD - inD;
  if (!Number.isFinite(ms) || ms <= 0) return 1;
  return Math.max(1, Math.ceil(ms / (1000 * 60 * 60 * 24)));
};

const ROOM_CATEGORIES = [
  { id: "standard", label: "Standard" },
  { id: "deluxe", label: "Deluxe" },
  { id: "suite", label: "Suite" },
];

const ID_TYPES = [
  { id: "passport", label: "Passport" },
  { id: "drivinglicense", label: "Driving License" },
  { id: "nationalid", label: "National ID" },
  { id: "visa", label: "Visa" },
  { id: "aadhaar", label: "Aadhaar (IN)" },
];

const PAYMENT_METHODS = [
  { id: "cash", label: "Cash" },
  { id: "card", label: "Card" },
  { id: "upi", label: "UPI" },
  { id: "bank", label: "Bank Transfer" },
  { id: "corporate", label: "Corporate Account" },
];

const RATE_PLANS = [
  { id: "rack", name: "Rack Rate", discount: 0, description: "Standard published rate" },
  { id: "walkin", name: "Walk-in Special", discount: 10, description: "Special rate for walk-ins" },
  { id: "corporate", name: "Corporate Rate", discount: 15, description: "For business travelers" },
  { id: "promo", name: "Promotional", discount: 20, description: "Limited time offer" },
];

const VALID_DISCOUNT_CODES = {
  WALKIN15: 15,
  SUMMER20: 20,
  FIRSTTIME10: 10,
  VIP25: 25,
};

const MOCK_GUESTS = [
  { id: "guest1", name: "John Doe", email: "john@example.com", phone: "1234567890", loyalty: "GOLD12345", nationality: "US", dob: "1985-01-15" },
  { id: "guest2", name: "Jane Smith", email: "jane@company.com", phone: "9876543210", loyalty: "PLAT67890", nationality: "UK", dob: "1990-05-20" },
  { id: "guest3", name: "Amit Patil", email: "amit.patil@gmail.com", phone: "9023456789", loyalty: "SILV11223", nationality: "IN", dob: "1993-08-10" },
];

const MOCK_COMPANIES = [
  { id: "co1", name: "Acme Corporation", taxId: "GSTINACME1234", address: "Business Park, Sector 5, Mumbai", contactEmail: "travel@acme.com", contactPhone: "9000000001" },
  { id: "co2", name: "Globex India Pvt Ltd", taxId: "GSTINGLOBEX7788", address: "IT Hub, Pune", contactEmail: "hr@globex.in", contactPhone: "9000000002" },
  { id: "co3", name: "Initech Services", taxId: "GSTININIT3333", address: "Andheri East, Mumbai", contactEmail: "ops@initech.com", contactPhone: "9000000003" },
];

const AVAILABLE_ROOMS = [
  { number: "101", category: "standard", rate: 120, floor: 1, amenities: ["Queen Bed", "City View", "Free WiFi"], status: "available" },
  { number: "102", category: "standard", rate: 120, floor: 1, amenities: ["Queen Bed", "Quiet Side", "Free WiFi"], status: "available" },
  { number: "103", category: "standard", rate: 125, floor: 1, amenities: ["Twin Bed", "City View", "Free WiFi"], status: "cleaning" },
  { number: "201", category: "deluxe", rate: 180, floor: 2, amenities: ["King Bed", "Balcony", "Breakfast"], status: "available" },
  { number: "202", category: "deluxe", rate: 185, floor: 2, amenities: ["King Bed", "Sea View", "Breakfast"], status: "maintenance" },
  { number: "205", category: "deluxe", rate: 180, floor: 2, amenities: ["King Bed", "Sea View", "Breakfast"], status: "available" },
  { number: "301", category: "suite", rate: 280, floor: 3, amenities: ["Living Room", "Balcony", "Premium"], status: "available" },
  { number: "302", category: "suite", rate: 290, floor: 3, amenities: ["Living Room", "Jacuzzi", "Premium"], status: "cleaning" },
  { number: "303", category: "suite", rate: 275, floor: 3, amenities: ["Living Room", "Balcony", "Premium"], status: "available" },
];

const roomStatusCounts = (rooms) => {
  const acc = { available: 0, cleaning: 0, maintenance: 0 };
  rooms.forEach((r) => (acc[r.status] = (acc[r.status] || 0) + 1));
  return acc;
};

const validateFile = (file, { maxMB, types }) => {
  if (!file) return { ok: false, message: "No file selected." };
  const maxBytes = maxMB * 1024 * 1024;
  if (file.size > maxBytes) return { ok: false, message: `File size must be less than ${maxMB}MB.` };
  if (types?.length && !types.includes(file.type)) return { ok: false, message: "Invalid file type." };
  return { ok: true };
};

const SectionCard = ({ title, subtitle, icon, right, children }) => (
  <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm">
    <div className="px-5 py-4 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-lg bg-blue-50 dark:bg-blue-900/20 flex items-center justify-center">
          <Icon name={icon} className="text-blue-600 dark:text-blue-400" />
        </div>
        <div>
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">{title}</h3>
          {subtitle ? <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{subtitle}</p> : null}
        </div>
      </div>
      {right ? <div className="shrink-0">{right}</div> : null}
    </div>
    <div className="p-5">{children}</div>
  </div>
);

const Field = ({ label, required, hint, children }) => (
  <div className="space-y-1.5">
    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
      {label} {required ? <span className="text-red-500">*</span> : null}
    </label>
    {children}
    {hint ? <p className="text-xs text-gray-500 dark:text-gray-400">{hint}</p> : null}
  </div>
);

const Input = (props) => (
  <input
    {...props}
    className={[
      "w-full rounded-lg px-3.5 py-2.5 border border-gray-300 dark:border-gray-600",
      "bg-white dark:bg-gray-800 text-gray-900 dark:text-white",
      "focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/20 transition",
      props.className || "",
    ].join(" ")}
  />
);

const Select = (props) => (
  <select
    {...props}
    className={[
      "w-full rounded-lg px-3.5 py-2.5 border border-gray-300 dark:border-gray-600",
      "bg-white dark:bg-gray-800 text-gray-900 dark:text-white",
      "focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/20 transition",
      props.className || "",
    ].join(" ")}
  />
);

const Textarea = (props) => (
  <textarea
    {...props}
    className={[
      "w-full rounded-lg px-3.5 py-2.5 border border-gray-300 dark:border-gray-600",
      "bg-white dark:bg-gray-800 text-gray-900 dark:text-white",
      "focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-900/20 transition",
      props.className || "",
    ].join(" ")}
  />
);

const Chip = ({ color = "blue", children }) => {
  const map = {
    blue: "bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300",
    green: "bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-300",
    yellow: "bg-yellow-50 dark:bg-yellow-900/30 text-yellow-700 dark:text-yellow-300",
    red: "bg-red-50 dark:bg-red-900/30 text-red-700 dark:text-red-300",
    gray: "bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300",
    purple: "bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300",
    amber: "bg-amber-50 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300",
  };
  return <span className={`inline-flex items-center px-2 py-1 text-xs font-medium rounded-md ${map[color] || map.blue}`}>{children}</span>;
};

const Divider = () => <div className="h-px bg-gray-200 dark:bg-gray-700" />;

const DocBox = ({ title, required, uploadedName, onClear, children }) => (
  <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
    <div className="flex items-center justify-between mb-3">
      <div>
        <p className="font-medium text-gray-900 dark:text-white">
          {title} {required ? <span className="text-red-500">*</span> : null}
        </p>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">Upload or capture. Max 5MB.</p>
      </div>
      {uploadedName ? (
        <button onClick={onClear} className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-500">
          <Icon name="delete" />
        </button>
      ) : null}
    </div>
    <div>{children}</div>
    {uploadedName ? (
      <div className="mt-3 flex items-center justify-between rounded-lg bg-green-50 dark:bg-green-900/20 p-2.5">
        <div className="min-w-0">
          <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{uploadedName}</p>
          <p className="text-xs text-green-600 dark:text-green-400">Uploaded</p>
        </div>
        <Icon name="check_circle" className="text-green-500" />
      </div>
    ) : (
      <p className="mt-2 text-xs text-red-500">Required for completion.</p>
    )}
  </div>
);

export default function WalkInCheckIn() {
  const [mode, setMode] = useState("single"); // single | group | company
  const [stepIndex, setStepIndex] = useState(0);

  const [toast, setToast] = useState({ show: false, message: "", type: "info" });
  const showToast = (message, type = "info") => {
    setToast({ show: true, message, type });
    window.clearTimeout(showToast._t);
    showToast._t = window.setTimeout(() => setToast((t) => ({ ...t, show: false })), 2800);
  };

  const [showRoomModal, setShowRoomModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);

  // Camera
  const [camera, setCamera] = useState({ open: false, target: null }); // target: { kind, memberId? }
  const videoRef = useRef(null);
  const streamRef = useRef(null);

  const stopCamera = async () => {
    try {
      const s = streamRef.current;
      if (s) s.getTracks().forEach((t) => t.stop());
    } catch (_) {}
    streamRef.current = null;
    if (videoRef.current) videoRef.current.srcObject = null;
  };

  const openCamera = async (target) => {
    setCamera({ open: true, target });
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "user" }, audio: false });
      streamRef.current = stream;
      if (videoRef.current) videoRef.current.srcObject = stream;
    } catch (e) {
      showToast("Camera not available. Use upload instead.", "warning");
      setCamera({ open: false, target: null });
    }
  };

  const capturePhoto = async () => {
    if (!videoRef.current) return;
    const v = videoRef.current;
    const canvas = document.createElement("canvas");
    canvas.width = v.videoWidth || 640;
    canvas.height = v.videoHeight || 480;
    const ctx = canvas.getContext("2d");
    ctx.drawImage(v, 0, 0, canvas.width, canvas.height);

    const blob = await new Promise((resolve) => canvas.toBlob(resolve, "image/jpeg", 0.92));
    if (!blob) return;

    const url = URL.createObjectURL(blob);
    const name = `photo-${new Date().toISOString().replace(/[:.]/g, "-")}.jpg`;

    const setPhotoToMember = (setFn, memberId, kindKey) => {
      setFn((p) => ({
        ...p,
        members: p.members.map((m) =>
          m.id === memberId
            ? { ...m, documents: { ...m.documents, guestPhoto: url, guestPhotoName: name, guestPhotoBlob: blob } }
            : m
        ),
      }));
      showToast("Member photo captured.", "success");
    };

    if (camera.target?.kind === "singleGuestPhoto") {
      setSingle((p) => ({ ...p, documents: { ...p.documents, guestPhoto: url, guestPhotoName: name, guestPhotoBlob: blob } }));
      showToast("Guest photo captured.", "success");
    } else if (camera.target?.kind === "groupLeaderPhoto") {
      setGroup((p) => ({ ...p, leader: { ...p.leader, documents: { ...p.leader.documents, guestPhoto: url, guestPhotoName: name, guestPhotoBlob: blob } } }));
      showToast("Leader photo captured.", "success");
    } else if (camera.target?.kind === "companyLeaderPhoto") {
      setCompany((p) => ({ ...p, leader: { ...p.leader, documents: { ...p.leader.documents, guestPhoto: url, guestPhotoName: name, guestPhotoBlob: blob } } }));
      showToast("Contact photo captured.", "success");
    } else if (camera.target?.kind === "groupMemberPhoto" && camera.target?.memberId) {
      setPhotoToMember(setGroup, camera.target.memberId, "group");
    } else if (camera.target?.kind === "companyMemberPhoto" && camera.target?.memberId) {
      setPhotoToMember(setCompany, camera.target.memberId, "company");
    }

    await stopCamera();
    setCamera({ open: false, target: null });
  };

  // Meta
  const [meta, setMeta] = useState({
    referenceSource: "walkin",
    discountCode: "",
    manualDiscountPercent: 0,
    notes: "",
    includeTax: true,
  });

  // Search states
  const [singleSearch, setSingleSearch] = useState({ query: "", results: [], open: false });
  const [groupSearch, setGroupSearch] = useState({ query: "", results: [], open: false });
  const [companySearch, setCompanySearch] = useState({ query: "", results: [], open: false });

  // Single booking
  const [single, setSingle] = useState({
    guest: {
      firstName: "",
      lastName: "",
      email: "",
      countryCode: "+91",
      phone: "",
      nationality: "IN",
      dob: "",
      address: "",
      loyaltyNumber: "",
      idType: "aadhaar",
      idNumber: "",
    },
    documents: {
      idProof: null,
      idProofName: "",
      idProofBlob: null,
      guestPhoto: null,
      guestPhotoName: "",
      guestPhotoBlob: null,
    },
    stay: {
      checkIn: todayISO(),
      checkOut: addDaysISO(1),
      nights: 1,
      rooms: 1,
      adults: 2,
      children: 0,
      specialRequests: "",
    },
    roomSelection: {
      category: "",
      selectedRoomNo: "",
    },
    pricing: {
      ratePlanId: "walkin",
      services: { parking: false, breakfast: false, lateCheckout: false },
    },
    payment: {
      method: "",
      amountPaid: "",
      txnRef: "",
      payerName: "",
    },
  });

  // Group booking
  const [group, setGroup] = useState({
    leader: {
      info: { firstName: "", lastName: "", email: "", countryCode: "+91", phone: "", nationality: "IN", dob: "", address: "", idType: "aadhaar", idNumber: "" },
      documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
    },
    members: [],
    stay: { checkIn: todayISO(), checkOut: addDaysISO(1), nights: 1, rooms: 2, adults: 2, children: 0, specialRequests: "" },
    roomSelection: { category: "", selectedRooms: [], assignments: {} },
    pricing: { ratePlanId: "walkin", services: { parking: false, breakfast: false, lateCheckout: false } },
    payment: { method: "", amountPaid: "", txnRef: "", payerName: "" },
  });

  // Company booking
  const [company, setCompany] = useState({
    company: { name: "", taxId: "", address: "", contactEmail: "", contactPhone: "" },
    leader: {
      info: { firstName: "", lastName: "", email: "", countryCode: "+91", phone: "", idType: "aadhaar", idNumber: "", address: "" },
      documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
    },
    members: [],
    stay: { checkIn: todayISO(), checkOut: addDaysISO(1), nights: 1, rooms: 2, adults: 2, children: 0, specialRequests: "" },
    roomSelection: { category: "", selectedRooms: [], assignments: {} },
    pricing: { ratePlanId: "corporate", services: { parking: false, breakfast: false, lateCheckout: false } },
    payment: { method: "", amountPaid: "", txnRef: "", payerName: "", billToCompany: true },
  });

  // Steps
  const steps = useMemo(() => {
    if (mode === "single") {
      return [
        { key: "search", title: "Search Guest", icon: "person_search" },
        { key: "guest", title: "Guest Info", icon: "person" },
        { key: "docs", title: "Documents", icon: "description" },
        { key: "stay", title: "Stay Details", icon: "calendar_month" },
        { key: "room", title: "Room Selection", icon: "bed" },
        { key: "pricing", title: "Pricing & Discount", icon: "sell" },
        { key: "billing", title: "Billing Review", icon: "receipt_long" },
        { key: "payment", title: "Payment", icon: "payments" },
      ];
    }
    if (mode === "group") {
      return [
        { key: "search", title: "Search Leader", icon: "person_search" },
        { key: "leader", title: "Leader Info", icon: "person" },
        { key: "docs", title: "Leader Documents", icon: "description" },
        { key: "members", title: "Members & Docs", icon: "groups" },
        { key: "stay", title: "Stay Details", icon: "calendar_month" },
        { key: "room", title: "Rooms & Assign", icon: "bed" },
        { key: "pricing", title: "Pricing & Discount", icon: "sell" },
        { key: "billing", title: "Billing Review", icon: "receipt_long" },
        { key: "payment", title: "Payment", icon: "payments" },
      ];
    }
    return [
      { key: "search", title: "Search Company", icon: "domain_search" },
      { key: "company", title: "Company Info", icon: "corporate_fare" },
      { key: "leader", title: "Contact Info", icon: "person" },
      { key: "docs", title: "Contact Documents", icon: "description" },
      { key: "members", title: "Members & Docs", icon: "groups" },
      { key: "stay", title: "Stay Details", icon: "calendar_month" },
      { key: "room", title: "Rooms & Assign", icon: "bed" },
      { key: "pricing", title: "Pricing & Discount", icon: "sell" },
      { key: "billing", title: "Billing Review", icon: "receipt_long" },
      { key: "payment", title: "Payment", icon: "payments" },
    ];
  }, [mode]);

  useEffect(() => setStepIndex(0), [mode]);

  // Nights auto
  useEffect(() => {
    setSingle((p) => ({ ...p, stay: { ...p.stay, nights: safeNights(p.stay.checkIn, p.stay.checkOut) } }));
  }, [single.stay.checkIn, single.stay.checkOut]);

  useEffect(() => {
    setGroup((p) => ({ ...p, stay: { ...p.stay, nights: safeNights(p.stay.checkIn, p.stay.checkOut) } }));
  }, [group.stay.checkIn, group.stay.checkOut]);

  useEffect(() => {
    setCompany((p) => ({ ...p, stay: { ...p.stay, nights: safeNights(p.stay.checkIn, p.stay.checkOut) } }));
  }, [company.stay.checkIn, company.stay.checkOut]);

  const status = useMemo(() => roomStatusCounts(AVAILABLE_ROOMS), []);
  const progressPct = useMemo(() => (steps.length ? Math.round(((stepIndex + 1) / steps.length) * 100) : 0), [stepIndex, steps.length]);

  // Helpers: selected rooms
  const selectedRoomObjects = useMemo(() => {
    if (mode === "single") {
      const no = single.roomSelection.selectedRoomNo;
      return no ? AVAILABLE_ROOMS.filter((r) => r.number === no) : [];
    }
    if (mode === "group") return AVAILABLE_ROOMS.filter((r) => group.roomSelection.selectedRooms.includes(r.number));
    return AVAILABLE_ROOMS.filter((r) => company.roomSelection.selectedRooms.includes(r.number));
  }, [mode, single.roomSelection.selectedRoomNo, group.roomSelection.selectedRooms, company.roomSelection.selectedRooms]);

  const stayInfo = useMemo(() => (mode === "single" ? single.stay : mode === "group" ? group.stay : company.stay), [mode, single.stay, group.stay, company.stay]);
  const servicesInfo = useMemo(() => (mode === "single" ? single.pricing.services : mode === "group" ? group.pricing.services : company.pricing.services), [mode, single.pricing.services, group.pricing.services, company.pricing.services]);

  const activeRatePlan = useMemo(() => {
    const planId = mode === "single" ? single.pricing.ratePlanId : mode === "group" ? group.pricing.ratePlanId : company.pricing.ratePlanId;
    return RATE_PLANS.find((p) => p.id === planId) || RATE_PLANS[0];
  }, [mode, single.pricing.ratePlanId, group.pricing.ratePlanId, company.pricing.ratePlanId]);

  const guestCountForBreakfast = useMemo(() => {
    if (mode === "single") return (single.stay.adults || 0) + (single.stay.children || 0);
    if (mode === "group") return Math.max(group.members.length || 0, (group.stay.rooms || 1) * (group.stay.adults || 1));
    return Math.max(company.members.length || 0, (company.stay.rooms || 1) * (company.stay.adults || 1));
  }, [mode, single.stay.adults, single.stay.children, group.members.length, group.stay.rooms, group.stay.adults, company.members.length, company.stay.rooms, company.stay.adults]);

  const discountPercent = useMemo(() => {
    const codePct = meta.discountCode?.trim() ? VALID_DISCOUNT_CODES[meta.discountCode.trim().toUpperCase()] || 0 : 0;
    const combined = clamp((activeRatePlan.discount || 0) + (meta.manualDiscountPercent || 0), 0, 80);
    return clamp(Math.max(combined, codePct), 0, 80);
  }, [activeRatePlan.discount, meta.discountCode, meta.manualDiscountPercent]);

  const totals = useMemo(() => {
    const nights = stayInfo.nights || 1;
    const roomCharges = selectedRoomObjects.reduce((sum, r) => sum + (r.rate || 0) * nights, 0);
    const discountAmount = (roomCharges * (discountPercent || 0)) / 100;
    const roomAfterDiscount = Math.max(0, roomCharges - discountAmount);

    const roomsCount = mode === "single" ? 1 : selectedRoomObjects.length || 0;

    let servicesTotal = 0;
    if (servicesInfo.parking) servicesTotal += 15 * nights * Math.max(1, roomsCount);
    if (servicesInfo.breakfast) servicesTotal += 18 * nights * Math.max(1, guestCountForBreakfast);
    if (servicesInfo.lateCheckout) servicesTotal += 30;

    const taxableBase = roomAfterDiscount + servicesTotal;
    const taxRate = meta.includeTax ? 0.1 : 0;
    const taxes = taxableBase * taxRate;
    const total = taxableBase + taxes;

    return { nights, roomsCount, roomCharges, discountAmount, servicesTotal, taxes, total };
  }, [stayInfo.nights, selectedRoomObjects, discountPercent, servicesInfo, meta.includeTax, mode, guestCountForBreakfast]);

  // Search (mock)
  const doGuestSearch = (query, setState) => {
    const q = (query || "").trim().toLowerCase();
    if (q.length < 2) return setState((p) => ({ ...p, query, open: false, results: [] }));
    const results = MOCK_GUESTS.filter((g) => `${g.name} ${g.email} ${g.phone} ${g.loyalty}`.toLowerCase().includes(q)).slice(0, 8);
    setState((p) => ({ ...p, query, open: true, results }));
  };

  const doCompanySearch = (query) => {
    const q = (query || "").trim().toLowerCase();
    if (q.length < 2) return setCompanySearch((p) => ({ ...p, query, open: false, results: [] }));
    const results = MOCK_COMPANIES.filter((c) => `${c.name} ${c.taxId} ${c.address} ${c.contactEmail} ${c.contactPhone}`.toLowerCase().includes(q)).slice(0, 8);
    setCompanySearch((p) => ({ ...p, query, open: true, results }));
  };

  const selectGuestIntoSingle = (g) => {
    const parts = (g.name || "").split(" ");
    setSingle((p) => ({
      ...p,
      guest: {
        ...p.guest,
        firstName: parts[0] || "",
        lastName: parts.slice(1).join(" ") || "",
        email: g.email || "",
        phone: g.phone || "",
        loyaltyNumber: g.loyalty || "",
        nationality: g.nationality || "IN",
        dob: g.dob || "",
      },
    }));
    setSingleSearch((p) => ({ ...p, open: false }));
    showToast(`Guest "${g.name}" loaded.`, "success");
  };

  const selectGuestIntoGroupLeader = (g) => {
    const parts = (g.name || "").split(" ");
    setGroup((p) => ({
      ...p,
      leader: {
        ...p.leader,
        info: {
          ...p.leader.info,
          firstName: parts[0] || "",
          lastName: parts.slice(1).join(" ") || "",
          email: g.email || "",
          phone: g.phone || "",
          nationality: g.nationality || "IN",
          dob: g.dob || "",
        },
      },
    }));
    setGroupSearch((p) => ({ ...p, open: false }));
    showToast(`Leader "${g.name}" loaded.`, "success");
  };

  const selectCompany = (c) => {
    setCompany((p) => ({
      ...p,
      company: { name: c.name || "", taxId: c.taxId || "", address: c.address || "", contactEmail: c.contactEmail || "", contactPhone: c.contactPhone || "" },
      leader: { ...p.leader, info: { ...p.leader.info, email: c.contactEmail || p.leader.info.email, phone: c.contactPhone || p.leader.info.phone } },
      pricing: { ...p.pricing, ratePlanId: "corporate" },
    }));
    setCompanySearch((p) => ({ ...p, open: false }));
    showToast(`Company "${c.name}" loaded.`, "success");
  };

  // Upload helpers
  const onUploadSingle = (kind, file) => {
    if (!file) {
      setSingle((p) => ({ ...p, documents: { ...p.documents, [kind]: null, [`${kind}Name`]: "", [`${kind}Blob`]: null } }));
      return showToast("Cleared.", "warning");
    }
    const isPhoto = kind === "guestPhoto";
    const ok = validateFile(file, { maxMB: 5, types: isPhoto ? ["image/png", "image/jpeg", "image/jpg"] : ["image/png", "image/jpeg", "image/jpg", "application/pdf"] });
    if (!ok.ok) return showToast(ok.message, "warning");
    const url = URL.createObjectURL(file);
    setSingle((p) => ({ ...p, documents: { ...p.documents, [kind]: url, [`${kind}Name`]: file.name, [`${kind}Blob`]: file } }));
    showToast(`${isPhoto ? "Guest photo" : "ID proof"} uploaded.`, "success");
  };

  const onUploadLeader = (modeKey, kind, file) => {
    const setFn = modeKey === "group" ? setGroup : setCompany;
    if (!file) {
      setFn((p) => ({ ...p, leader: { ...p.leader, documents: { ...p.leader.documents, [kind]: null, [`${kind}Name`]: "", [`${kind}Blob`]: null } } }));
      return showToast("Cleared.", "warning");
    }
    const isPhoto = kind === "guestPhoto";
    const ok = validateFile(file, { maxMB: 5, types: isPhoto ? ["image/png", "image/jpeg", "image/jpg"] : ["image/png", "image/jpeg", "image/jpg", "application/pdf"] });
    if (!ok.ok) return showToast(ok.message, "warning");
    const url = URL.createObjectURL(file);
    setFn((p) => ({ ...p, leader: { ...p.leader, documents: { ...p.leader.documents, [kind]: url, [`${kind}Name`]: file.name, [`${kind}Blob`]: file } } }));
    showToast(`${isPhoto ? "Photo" : "ID proof"} uploaded.`, "success");
  };

  const clearMemberDoc = (modeKey, memberId, kind) => {
    const setFn = modeKey === "group" ? setGroup : setCompany;
    setFn((p) => ({
      ...p,
      members: p.members.map((m) =>
        m.id === memberId ? { ...m, documents: { ...m.documents, [kind]: null, [`${kind}Name`]: "", [`${kind}Blob`]: null } } : m
      ),
    }));
    showToast("Cleared.", "warning");
  };

  const onUploadMember = (modeKey, memberId, kind, file) => {
    if (!file) return clearMemberDoc(modeKey, memberId, kind);

    const isPhoto = kind === "guestPhoto";
    const ok = validateFile(file, { maxMB: 5, types: isPhoto ? ["image/png", "image/jpeg", "image/jpg"] : ["image/png", "image/jpeg", "image/jpg", "application/pdf"] });
    if (!ok.ok) return showToast(ok.message, "warning");

    const url = URL.createObjectURL(file);
    const setFn = modeKey === "group" ? setGroup : setCompany;
    setFn((p) => ({
      ...p,
      members: p.members.map((m) =>
        m.id === memberId
          ? { ...m, documents: { ...m.documents, [kind]: url, [`${kind}Name`]: file.name, [`${kind}Blob`]: file } }
          : m
      ),
    }));
    showToast(`${isPhoto ? "Member photo" : "Member ID proof"} uploaded.`, "success");
  };

  // Member management
  const addMember = (modeKey) => {
    const newM = {
      id: uid("member"),
      firstName: "",
      lastName: "",
      idType: "aadhaar",
      idNumber: "",
      roomNo: "",
      documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
    };
    if (modeKey === "group") setGroup((p) => ({ ...p, members: [...p.members, newM] }));
    else setCompany((p) => ({ ...p, members: [...p.members, newM] }));
    showToast("Member added.", "success");
  };

  const removeMember = (modeKey, memberId) => {
    if (!window.confirm("Delete this member?")) return;
    if (modeKey === "group") {
      setGroup((p) => ({
        ...p,
        members: p.members.filter((m) => m.id !== memberId),
        roomSelection: { ...p.roomSelection, assignments: { ...p.roomSelection.assignments, [memberId]: undefined } },
      }));
    } else {
      setCompany((p) => ({
        ...p,
        members: p.members.filter((m) => m.id !== memberId),
        roomSelection: { ...p.roomSelection, assignments: { ...p.roomSelection.assignments, [memberId]: undefined } },
      }));
    }
    showToast("Member removed.", "warning");
  };

  const updateMember = (modeKey, memberId, patch) => {
    const setFn = modeKey === "group" ? setGroup : setCompany;
    setFn((p) => ({ ...p, members: p.members.map((m) => (m.id === memberId ? { ...m, ...patch } : m)) }));
  };

  // Rooms
  const availableByCategory = (cat) => AVAILABLE_ROOMS.filter((r) => (cat ? r.category === cat : true) && r.status === "available");

  const toggleSelectedRoom = (modeKey, roomNo) => {
    const room = AVAILABLE_ROOMS.find((r) => r.number === roomNo);
    if (!room || room.status !== "available") return;
    const setFn = modeKey === "group" ? setGroup : setCompany;

    setFn((p) => {
      const needed = Math.max(1, p.stay.rooms || 1);
      const exists = p.roomSelection.selectedRooms.includes(roomNo);
      const next = exists ? p.roomSelection.selectedRooms.filter((x) => x !== roomNo) : [...p.roomSelection.selectedRooms, roomNo];
      const trimmed = next.slice(0, needed);

      const allowed = new Set(trimmed);
      const assignments = { ...p.roomSelection.assignments };
      Object.keys(assignments).forEach((mid) => {
        if (assignments[mid] && !allowed.has(assignments[mid])) assignments[mid] = "";
      });

      return { ...p, roomSelection: { ...p.roomSelection, selectedRooms: trimmed, assignments } };
    });
  };

  const assignMemberRoom = (modeKey, memberId, roomNo) => {
    const setFn = modeKey === "group" ? setGroup : setCompany;
    setFn((p) => ({
      ...p,
      roomSelection: { ...p.roomSelection, assignments: { ...p.roomSelection.assignments, [memberId]: roomNo } },
      members: p.members.map((m) => (m.id === memberId ? { ...m, roomNo } : m)),
    }));
  };

  const autoAssignMembers = (modeKey) => {
    const state = modeKey === "group" ? group : company;
    const selectedRooms = state.roomSelection.selectedRooms;
    if (!selectedRooms.length) return showToast("Select rooms first.", "warning");
    if (!state.members.length) return showToast("Add members first.", "warning");

    const assignments = {};
    state.members.forEach((m, idx) => (assignments[m.id] = selectedRooms[idx % selectedRooms.length]));
    const setFn = modeKey === "group" ? setGroup : setCompany;

    setFn((p) => ({
      ...p,
      roomSelection: { ...p.roomSelection, assignments },
      members: p.members.map((m) => ({ ...m, roomNo: assignments[m.id] || "" })),
    }));
    showToast("Auto-assignment complete.", "success");
  };

  // Navigation + validation
  const validateCurrentStep = () => {
    const step = steps[stepIndex]?.key;
    const req = (cond, message) => {
      if (cond) return true;
      showToast(message, "warning");
      return false;
    };

    if (mode === "single") {
      if (step === "guest") {
        const g = single.guest;
        return (
          req(g.firstName?.trim() && g.lastName?.trim(), "First & last name are required.") &&
          req(g.email?.trim(), "Email is required.") &&
          req(g.phone?.trim(), "Phone is required.") &&
          req(g.idNumber?.trim(), "ID number is required.")
        );
      }
      if (step === "docs") return req(single.documents.idProof, "ID proof required.") && req(single.documents.guestPhoto, "Guest photo required.");
      if (step === "stay") return req(new Date(single.stay.checkOut) > new Date(single.stay.checkIn), "Check-out must be after check-in.");
      if (step === "room") return req(single.roomSelection.category, "Select room category.") && req(single.roomSelection.selectedRoomNo, "Select room number.");
      if (step === "payment") {
        const p = single.payment;
        return (
          req(totals.total > 0, "Total must be > 0.") &&
          req(p.method, "Select payment method.") &&
          req(p.amountPaid !== "" && Number(p.amountPaid) >= totals.total, "Amount paid must be >= total.")
        );
      }
      return true;
    }

    if (mode === "group") {
      if (step === "leader") {
        const l = group.leader.info;
        return req(l.firstName?.trim() && l.lastName?.trim(), "Leader name required.") && req(l.phone?.trim(), "Leader phone required.") && req(l.idNumber?.trim(), "Leader ID required.");
      }
      if (step === "docs") return req(group.leader.documents.idProof, "Leader ID proof required.") && req(group.leader.documents.guestPhoto, "Leader photo required.");
      if (step === "members") return req(group.members.length > 0, "Add at least 1 member.");
      if (step === "stay") return req(new Date(group.stay.checkOut) > new Date(group.stay.checkIn), "Check-out must be after check-in.");
      if (step === "room") {
        const needed = Math.max(1, group.stay.rooms || 1);
        return (
          req(group.roomSelection.selectedRooms.length === needed, `Select exactly ${needed} rooms.`) &&
          req(group.members.every((m) => group.roomSelection.assignments[m.id]), "Assign every member to a room.")
        );
      }
      if (step === "payment") {
        const p = group.payment;
        return req(p.method, "Select payment method.") && req(p.amountPaid !== "" && Number(p.amountPaid) >= totals.total, "Amount paid must be >= total.");
      }
      return true;
    }

    // company
    if (step === "company") return req(company.company.name?.trim(), "Company name required.") && req(company.company.taxId?.trim(), "Company Tax ID/GST required.");
    if (step === "leader") return req(company.leader.info.firstName?.trim() && company.leader.info.lastName?.trim(), "Contact name required.") && req(company.leader.info.phone?.trim(), "Contact phone required.");
    if (step === "docs") return req(company.leader.documents.idProof, "Contact ID proof required.") && req(company.leader.documents.guestPhoto, "Contact photo required.");
    if (step === "members") return req(company.members.length > 0, "Add at least 1 member.");
    if (step === "stay") return req(new Date(company.stay.checkOut) > new Date(company.stay.checkIn), "Check-out must be after check-in.");
    if (step === "room") {
      const needed = Math.max(1, company.stay.rooms || 1);
      return (
        req(company.roomSelection.selectedRooms.length === needed, `Select exactly ${needed} rooms.`) &&
        req(company.members.every((m) => company.roomSelection.assignments[m.id]), "Assign every member to a room.")
      );
    }
    if (step === "payment") {
      const p = company.payment;
      if (p.method === "corporate" && p.billToCompany) return req(p.method, "Select payment method.");
      return req(p.method, "Select payment method.") && req(p.amountPaid !== "" && Number(p.amountPaid) >= totals.total, "Amount paid must be >= total.");
    }
    return true;
  };

  const goNext = () => {
    if (!validateCurrentStep()) return;
    setStepIndex((i) => clamp(i + 1, 0, steps.length - 1));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const goPrev = () => {
    setStepIndex((i) => clamp(i - 1, 0, steps.length - 1));
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const resetAll = () => {
    if (!window.confirm("Clear all form data and start a new booking?")) return;
    setMode("single");
    setStepIndex(0);
    setMeta({ referenceSource: "walkin", discountCode: "", manualDiscountPercent: 0, notes: "", includeTax: true });
    setSingleSearch({ query: "", results: [], open: false });
    setGroupSearch({ query: "", results: [], open: false });
    setCompanySearch({ query: "", results: [], open: false });

    setSingle({
      guest: { firstName: "", lastName: "", email: "", countryCode: "+91", phone: "", nationality: "IN", dob: "", address: "", loyaltyNumber: "", idType: "aadhaar", idNumber: "" },
      documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
      stay: { checkIn: todayISO(), checkOut: addDaysISO(1), nights: 1, rooms: 1, adults: 2, children: 0, specialRequests: "" },
      roomSelection: { category: "", selectedRoomNo: "" },
      pricing: { ratePlanId: "walkin", services: { parking: false, breakfast: false, lateCheckout: false } },
      payment: { method: "", amountPaid: "", txnRef: "", payerName: "" },
    });

    setGroup({
      leader: {
        info: { firstName: "", lastName: "", email: "", countryCode: "+91", phone: "", nationality: "IN", dob: "", address: "", idType: "aadhaar", idNumber: "" },
        documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
      },
      members: [],
      stay: { checkIn: todayISO(), checkOut: addDaysISO(1), nights: 1, rooms: 2, adults: 2, children: 0, specialRequests: "" },
      roomSelection: { category: "", selectedRooms: [], assignments: {} },
      pricing: { ratePlanId: "walkin", services: { parking: false, breakfast: false, lateCheckout: false } },
      payment: { method: "", amountPaid: "", txnRef: "", payerName: "" },
    });

    setCompany({
      company: { name: "", taxId: "", address: "", contactEmail: "", contactPhone: "" },
      leader: {
        info: { firstName: "", lastName: "", email: "", countryCode: "+91", phone: "", idType: "aadhaar", idNumber: "", address: "" },
        documents: { idProof: null, idProofName: "", idProofBlob: null, guestPhoto: null, guestPhotoName: "", guestPhotoBlob: null },
      },
      members: [],
      stay: { checkIn: todayISO(), checkOut: addDaysISO(1), nights: 1, rooms: 2, adults: 2, children: 0, specialRequests: "" },
      roomSelection: { category: "", selectedRooms: [], assignments: {} },
      pricing: { ratePlanId: "corporate", services: { parking: false, breakfast: false, lateCheckout: false } },
      payment: { method: "", amountPaid: "", txnRef: "", payerName: "", billToCompany: true },
    });

    showToast("Form cleared.", "warning");
  };

  const completeBooking = async () => {
    if (!validateCurrentStep()) return;

    const payload =
      mode === "single"
        ? { mode, meta, single, selectedRooms: selectedRoomObjects, totals, createdAt: new Date().toISOString() }
        : mode === "group"
        ? { mode, meta, group, selectedRooms: selectedRoomObjects, totals, createdAt: new Date().toISOString() }
        : { mode, meta, company, selectedRooms: selectedRoomObjects, totals, createdAt: new Date().toISOString() };

    console.log("BOOKING_SUBMIT", payload);
    showToast("Finalizing booking...", "info");
    await new Promise((r) => setTimeout(r, 900));
    setShowSuccessModal(true);
    showToast("Booking completed successfully!", "success");
  };

  // ========= UI parts =========
  const renderModeTabs = () => (
    <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-1.5">
      <div className="grid grid-cols-3 gap-1.5">
        <button
          onClick={() => setMode("single")}
          className={[
            "px-4 py-2.5 rounded-md font-medium text-sm transition-all flex items-center justify-center gap-2",
            mode === "single" ? "bg-blue-600 text-white shadow-sm" : "text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
          ].join(" ")}
        >
          <Icon name="person" />
          Single
        </button>
        <button
          onClick={() => setMode("group")}
          className={[
            "px-4 py-2.5 rounded-md font-medium text-sm transition-all flex items-center justify-center gap-2",
            mode === "group" ? "bg-purple-600 text-white shadow-sm" : "text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
          ].join(" ")}
        >
          <Icon name="group" />
          Group
        </button>
        <button
          onClick={() => setMode("company")}
          className={[
            "px-4 py-2.5 rounded-md font-medium text-sm transition-all flex items-center justify-center gap-2",
            mode === "company" ? "bg-amber-600 text-white shadow-sm" : "text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700",
          ].join(" ")}
        >
          <Icon name="corporate_fare" />
          Company
        </button>
      </div>
    </div>
  );

  const renderStepper = () => (
    <div className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
        <div className="flex items-center justify-between mb-3">
          <div>
            <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 mb-1.5">
              <Chip color="gray">
                Step {stepIndex + 1} / {steps.length}
              </Chip>
              <span className="font-medium text-gray-900 dark:text-white">{steps[stepIndex]?.title}</span>
            </div>
            <div className="h-1.5 rounded-full bg-gray-200 dark:bg-gray-800 overflow-hidden w-64">
              <div className="h-full rounded-full bg-gradient-to-r from-blue-600 to-violet-600" style={{ width: `${progressPct}%` }} />
            </div>
          </div>
          <button
            onClick={() => setShowRoomModal(true)}
            className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border border-gray-300 dark:border-gray-700 text-sm font-medium text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-800"
          >
            <Icon name="hotel" />
            Room Status
          </button>
        </div>

        <div className="flex overflow-x-auto gap-1 pb-1">
          {steps.map((s, idx) => (
            <button
              key={s.key}
              onClick={() => idx <= stepIndex && setStepIndex(idx)}
              className={[
                "flex items-center gap-2 px-3 py-2 rounded-md text-xs font-medium border transition shrink-0",
                idx === stepIndex
                  ? "border-blue-300 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300"
                  : idx < stepIndex
                  ? "border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700"
                  : "border-gray-200/70 dark:border-gray-700/60 bg-gray-50 dark:bg-gray-800/50 text-gray-400 dark:text-gray-500 cursor-not-allowed",
              ].join(" ")}
              disabled={idx > stepIndex}
              title={s.title}
            >
              <Icon name={s.icon} className="text-[16px]" />
              <span>{s.title}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );

  // ========= Step Renderers =========

  // --- Single Search
  const renderSingleSearch = () => (
    <SectionCard
      icon="person_search"
      title="Search guest details"
      subtitle="Search by name, phone, email, or loyalty to avoid duplicates."
      right={
        <button
          onClick={() => {
            setSingleSearch((p) => ({ ...p, open: false }));
            showToast("Proceeding without search.", "info");
            goNext();
          }}
          className="text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white"
        >
          Skip
        </button>
      }
    >
      <div className="relative">
        <div className="flex items-center gap-3 bg-gray-50 dark:bg-gray-800 rounded-lg px-3.5 py-2.5 border border-gray-200 dark:border-gray-700 focus-within:ring-2 focus-within:ring-blue-100 dark:focus-within:ring-blue-900/20">
          <Icon name="search" className="text-gray-400" />
          <Input
            placeholder="Search guest..."
            value={singleSearch.query}
            onChange={(e) => doGuestSearch(e.target.value, setSingleSearch)}
            onFocus={() => setSingleSearch((p) => ({ ...p, open: p.results.length > 0 }))}
            className="border-0 bg-transparent px-0 py-0 focus:ring-0 focus:border-0"
          />
        </div>

        {singleSearch.open ? (
          <div className="absolute z-40 w-full mt-1.5 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-lg max-h-72 overflow-y-auto">
            {singleSearch.results.length ? (
              singleSearch.results.map((g) => (
                <button
                  key={g.id}
                  className="w-full text-left p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-100 dark:border-gray-700 last:border-b-0"
                  onClick={() => selectGuestIntoSingle(g)}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="font-medium text-gray-900 dark:text-white truncate">{g.name}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400 truncate">
                        {g.email} • {g.phone}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5 truncate">Loyalty: {g.loyalty}</p>
                    </div>
                    <Icon name="arrow_forward" className="text-gray-400" />
                  </div>
                </button>
              ))
            ) : (
              <div className="p-3 text-sm text-gray-600 dark:text-gray-400">No matches.</div>
            )}

            <div className="p-3">
              <button
                onClick={() => {
                  setSingleSearch((p) => ({ ...p, open: false }));
                  goNext();
                }}
                className="w-full px-4 py-2.5 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 transition"
              >
                Create / Continue with new guest
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </SectionCard>
  );

  // --- Single Guest Info
  const renderSingleGuestInfo = () => (
    <SectionCard icon="person" title="Guest info" subtitle="Enter guest information for check-in.">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="First name" required>
          <Input value={single.guest.firstName} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, firstName: e.target.value } }))} />
        </Field>
        <Field label="Last name" required>
          <Input value={single.guest.lastName} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, lastName: e.target.value } }))} />
        </Field>
        <Field label="Email" required>
          <Input type="email" value={single.guest.email} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, email: e.target.value } }))} />
        </Field>
        <Field label="Phone" required>
          <div className="flex gap-2">
            <Select value={single.guest.countryCode} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, countryCode: e.target.value } }))} className="w-28">
              <option value="+91">+91 IN</option>
              <option value="+1">+1 US</option>
              <option value="+44">+44 UK</option>
              <option value="+61">+61 AU</option>
            </Select>
            <Input value={single.guest.phone} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, phone: e.target.value } }))} />
          </div>
        </Field>

        <Field label="Nationality">
          <Select value={single.guest.nationality} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, nationality: e.target.value } }))}>
            <option value="IN">India</option>
            <option value="US">United States</option>
            <option value="UK">United Kingdom</option>
            <option value="CA">Canada</option>
            <option value="AU">Australia</option>
          </Select>
        </Field>

        <Field label="Date of birth">
          <Input type="date" value={single.guest.dob} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, dob: e.target.value } }))} />
        </Field>

        <Field label="Address" required>
          <Textarea rows={2} value={single.guest.address} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, address: e.target.value } }))} />
        </Field>

        <Field label="Loyalty number">
          <Input value={single.guest.loyaltyNumber} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, loyaltyNumber: e.target.value } }))} />
        </Field>

        <Field label="ID type" required>
          <Select value={single.guest.idType} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, idType: e.target.value } }))}>
            {ID_TYPES.map((t) => (
              <option key={t.id} value={t.id}>
                {t.label}
              </option>
            ))}
          </Select>
        </Field>

        <Field label="ID number" required>
          <Input value={single.guest.idNumber} onChange={(e) => setSingle((p) => ({ ...p, guest: { ...p.guest, idNumber: e.target.value } }))} />
        </Field>
      </div>
    </SectionCard>
  );

  // --- Single Docs
  const renderSingleDocs = () => (
    <SectionCard icon="description" title="Guest documentation" subtitle="Upload ID proof and capture/upload guest photo (mandatory).">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <DocBox title="ID proof" required uploadedName={single.documents.idProofName} onClear={() => onUploadSingle("idProof", null)}>
          <label className="w-full cursor-pointer">
            <div className="rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-4 text-center transition">
              <Icon name="cloud_upload" className="text-gray-500" />
              <p className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload ID document</p>
              <p className="text-xs text-gray-500 dark:text-gray-400">PNG / JPG / PDF</p>
            </div>
            <input type="file" className="hidden" accept=".png,.jpg,.jpeg,.pdf" onChange={(e) => e.target.files?.[0] && onUploadSingle("idProof", e.target.files[0])} />
          </label>
        </DocBox>

        <DocBox title="Guest photo" required uploadedName={single.documents.guestPhotoName} onClear={() => onUploadSingle("guestPhoto", null)}>
          <div className="grid grid-cols-2 gap-2">
            <button
              onClick={() => openCamera({ kind: "singleGuestPhoto" })}
              className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center"
            >
              <Icon name="photo_camera" className="text-gray-700 dark:text-gray-200" />
              <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Use camera</span>
            </button>

            <label className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center cursor-pointer">
              <Icon name="upload" className="text-gray-700 dark:text-gray-200" />
              <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload photo</span>
              <input type="file" className="hidden" accept="image/png,image/jpeg,image/jpg" onChange={(e) => e.target.files?.[0] && onUploadSingle("guestPhoto", e.target.files[0])} />
            </label>
          </div>
        </DocBox>
      </div>
    </SectionCard>
  );

  // --- Stay details (shared)
  const renderStayDetails = (modeKey) => {
    const isSingle = modeKey === "single";
    const isGroup = modeKey === "group";
    const state = isSingle ? single : isGroup ? group : company;
    const setState = isSingle ? setSingle : isGroup ? setGroup : setCompany;

    const setNightsQuick = (n) => {
      const inD = new Date(state.stay.checkIn || todayISO());
      const out = new Date(inD);
      out.setDate(out.getDate() + n);
      setState((p) => ({ ...p, stay: { ...p.stay, checkIn: inD.toISOString().split("T")[0], checkOut: out.toISOString().split("T")[0], nights: n } }));
      showToast(`${n} night stay selected.`, "success");
    };

    return (
      <SectionCard icon="calendar_month" title="Stay details" subtitle="Select dates, rooms, occupancy, and preferences.">
        <div className="flex flex-wrap gap-1.5 mb-4">
          <button onClick={() => setNightsQuick(1)} className="px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm">
            Tonight
          </button>
          <button onClick={() => setNightsQuick(2)} className="px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm">
            2 Nights
          </button>
          <button onClick={() => setNightsQuick(3)} className="px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm">
            3 Nights
          </button>
          <button onClick={() => setNightsQuick(7)} className="px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm">
            1 Week
          </button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Field label="Check-in date" required>
            <Input type="date" value={state.stay.checkIn} onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, checkIn: e.target.value } }))} />
          </Field>
          <Field label="Check-out date" required>
            <Input type="date" value={state.stay.checkOut} onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, checkOut: e.target.value } }))} />
          </Field>

          <Field label="Rooms" required hint="For Group/Company: select multiple rooms later and assign members.">
            <div className="flex items-center rounded-lg border border-gray-300 dark:border-gray-700 overflow-hidden">
              <button onClick={() => setState((p) => ({ ...p, stay: { ...p.stay, rooms: Math.max(1, (p.stay.rooms || 1) - 1) } }))} className="w-10 h-10 flex items-center justify-center bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700">
                <Icon name="remove" />
              </button>
              <input
                type="number"
                min={1}
                max={10}
                value={state.stay.rooms}
                onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, rooms: clamp(parseInt(e.target.value || "1", 10), 1, 10) } }))}
                className="flex-1 text-center bg-transparent outline-none font-medium text-gray-900 dark:text-white"
              />
              <button onClick={() => setState((p) => ({ ...p, stay: { ...p.stay, rooms: Math.min(10, (p.stay.rooms || 1) + 1) } }))} className="w-10 h-10 flex items-center justify-center bg-gray-50 dark:bg-gray-800 hover:bg-gray-100 dark:hover:bg-gray-700">
                <Icon name="add" />
              </button>
            </div>
          </Field>

          <Field label="Guests per room">
            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1.5">Adults</label>
                <Select value={state.stay.adults} onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, adults: parseInt(e.target.value, 10) } }))}>
                  {[1, 2, 3, 4].map((n) => (
                    <option key={n} value={n}>
                      {n} Adult{n > 1 ? "s" : ""}
                    </option>
                  ))}
                </Select>
              </div>
              <div>
                <label className="block text-xs text-gray-500 dark:text-gray-400 mb-1.5">Children</label>
                <Select value={state.stay.children} onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, children: parseInt(e.target.value, 10) } }))}>
                  {[0, 1, 2, 3].map((n) => (
                    <option key={n} value={n}>
                      {n} Child{n === 1 ? "" : "ren"}
                    </option>
                  ))}
                </Select>
              </div>
            </div>
          </Field>

          <div className="md:col-span-2">
            <Field label="Special requests">
              <Textarea rows={3} value={state.stay.specialRequests} onChange={(e) => setState((p) => ({ ...p, stay: { ...p.stay, specialRequests: e.target.value } }))} />
            </Field>
          </div>
        </div>
      </SectionCard>
    );
  };

  // --- Single Room selection
  const renderSingleRoomSelection = () => {
    const categoryRooms = availableByCategory(single.roomSelection.category);
    return (
      <SectionCard icon="bed" title="Room category & number" subtitle="Choose a room category, then select an available room number.">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-5">
          <Field label="Room category" required>
            <Select
              value={single.roomSelection.category}
              onChange={(e) => setSingle((p) => ({ ...p, roomSelection: { ...p.roomSelection, category: e.target.value, selectedRoomNo: "" } }))}
            >
              <option value="">Select category</option>
              {ROOM_CATEGORIES.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label}
                </option>
              ))}
            </Select>
          </Field>

          <Field label="Room number" required>
            <Select
              value={single.roomSelection.selectedRoomNo}
              onChange={(e) => setSingle((p) => ({ ...p, roomSelection: { ...p.roomSelection, selectedRoomNo: e.target.value } }))}
              disabled={!single.roomSelection.category}
            >
              <option value="">{single.roomSelection.category ? "Select room number" : "Select category first"}</option>
              {categoryRooms.map((r) => (
                <option key={r.number} value={r.number}>
                  Room {r.number} • ₹{r.rate}/night • Floor {r.floor}
                </option>
              ))}
            </Select>
          </Field>
        </div>

        <div>
          <p className="text-sm font-semibold text-gray-900 dark:text-white mb-2.5">Available rooms</p>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {AVAILABLE_ROOMS.filter((r) => (single.roomSelection.category ? r.category === single.roomSelection.category : true)).map((room) => {
              const selected = single.roomSelection.selectedRoomNo === room.number;
              const disabled = room.status !== "available";
              return (
                <button
                  key={room.number}
                  onClick={() => {
                    if (disabled) return;
                    setSingle((p) => ({ ...p, roomSelection: { ...p.roomSelection, selectedRoomNo: room.number } }));
                    showToast(`Room ${room.number} selected.`, "success");
                  }}
                  className={[
                    "text-left rounded-lg border p-3 transition text-sm",
                    selected ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20" : "border-gray-200 dark:border-gray-700 hover:border-blue-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                    disabled ? "opacity-50 cursor-not-allowed" : "",
                  ].join(" ")}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-medium text-gray-900 dark:text-white">Room {room.number}</p>
                      <p className="text-gray-600 dark:text-gray-400 mt-0.5">
                        {ROOM_CATEGORIES.find((c) => c.id === room.category)?.label} • Floor {room.floor}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-900 dark:text-white">₹{room.rate}</p>
                      <div className="mt-1">
                        {room.status === "available" ? <Chip color="green">Available</Chip> : room.status === "cleaning" ? <Chip color="yellow">Cleaning</Chip> : <Chip color="red">Maintenance</Chip>}
                      </div>
                    </div>
                  </div>
                  <div className="mt-2.5 space-y-1">
                    {room.amenities.slice(0, 3).map((a) => (
                      <div key={a} className="flex items-center text-gray-600 dark:text-gray-400">
                        <Icon name="check" className="text-[16px] text-green-500" />
                        <span className="ml-1.5 text-xs">{a}</span>
                      </div>
                    ))}
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </SectionCard>
    );
  };

  // --- Multi-room selection (Group/Company)
  const renderMultiRoomSelection = (modeKey) => {
    const state = modeKey === "group" ? group : company;
    const setState = modeKey === "group" ? setGroup : setCompany;

    const needed = Math.max(1, state.stay.rooms || 1);
    const selected = state.roomSelection.selectedRooms;

    const filtered = AVAILABLE_ROOMS.filter((r) => r.status === "available" && (state.roomSelection.category ? r.category === state.roomSelection.category : true));

    return (
      <SectionCard
        icon="bed"
        title="Multiple rooms selection & assignment"
        subtitle={`Select exactly ${needed} rooms and assign each member to a room.`}
        right={
          <div className="flex items-center gap-1.5">
            <button onClick={() => autoAssignMembers(modeKey)} className="px-3 py-1.5 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 text-sm">
              Auto-assign
            </button>
            <button
              onClick={() => {
                setState((p) => ({ ...p, roomSelection: { ...p.roomSelection, selectedRooms: [], assignments: {} }, members: p.members.map((m) => ({ ...m, roomNo: "" })) }));
                showToast("Room selection cleared.", "warning");
              }}
              className="px-3 py-1.5 rounded-lg border border-gray-300 dark:border-gray-700 text-sm font-medium hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              Clear
            </button>
          </div>
        }
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-5">
          <Field label="Room category filter">
            <Select value={state.roomSelection.category} onChange={(e) => setState((p) => ({ ...p, roomSelection: { ...p.roomSelection, category: e.target.value } }))}>
              <option value="">All categories</option>
              {ROOM_CATEGORIES.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label}
                </option>
              ))}
            </Select>
          </Field>

          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 p-3">
            <p className="text-sm font-medium text-gray-900 dark:text-white">Selection</p>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-0.5">
              Selected {selected.length} / {needed}
            </p>
            <div className="mt-2 flex flex-wrap gap-1.5">
              {selected.length ? selected.map((r) => <Chip key={r} color="blue">Room {r}</Chip>) : <Chip color="gray">None</Chip>}
            </div>
          </div>
        </div>

        <div className="mb-6">
          <p className="text-sm font-medium text-gray-900 dark:text-white mb-2.5">Tap to select rooms</p>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            {filtered.map((room) => {
              const isSel = selected.includes(room.number);
              return (
                <button
                  key={room.number}
                  onClick={() => toggleSelectedRoom(modeKey, room.number)}
                  className={[
                    "text-left rounded-lg border p-3 transition text-sm",
                    isSel ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20" : "border-gray-200 dark:border-gray-700 hover:border-blue-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                    !isSel && selected.length >= needed ? "opacity-60 cursor-not-allowed" : "",
                  ].join(" ")}
                  disabled={!isSel && selected.length >= needed}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="font-medium text-gray-900 dark:text-white">Room {room.number}</p>
                      <p className="text-gray-600 dark:text-gray-400 mt-0.5">
                        {ROOM_CATEGORIES.find((c) => c.id === room.category)?.label} • Floor {room.floor}
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-900 dark:text-white">₹{room.rate}</p>
                      <div className="mt-1">{isSel ? <Chip color="green">Selected</Chip> : <Chip color="gray">Available</Chip>}</div>
                    </div>
                  </div>
                  <div className="mt-2.5 space-y-1">
                    {room.amenities.slice(0, 2).map((a) => (
                      <div key={a} className="flex items-center text-gray-600 dark:text-gray-400">
                        <Icon name="check" className="text-[16px] text-green-500" />
                        <span className="ml-1.5 text-xs">{a}</span>
                      </div>
                    ))}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        <Divider />
        <div className="mt-5">
          <div className="flex items-center justify-between gap-4 mb-3">
            <div>
              <p className="text-sm font-medium text-gray-900 dark:text-white">Assign members to rooms</p>
              <p className="text-sm text-gray-600 dark:text-gray-400">Every member must be assigned to one of the selected rooms.</p>
            </div>
            <Chip color={modeKey === "group" ? "purple" : "amber"}>{state.members.length} member(s)</Chip>
          </div>

          <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-700">
            <table className="min-w-full">
              <thead className="bg-gray-50 dark:bg-gray-800">
                <tr>
                  <th className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 px-3 py-2">Member</th>
                  <th className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 px-3 py-2">ID</th>
                  <th className="text-left text-xs font-medium text-gray-500 dark:text-gray-400 px-3 py-2">Room</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 dark:divide-gray-700">
                {state.members.map((m) => (
                  <tr key={m.id}>
                    <td className="px-3 py-2 text-sm text-gray-900 dark:text-white">{m.firstName || m.lastName ? `${m.firstName} ${m.lastName}` : <span className="text-gray-400">Unnamed</span>}</td>
                    <td className="px-3 py-2 text-sm text-gray-600 dark:text-gray-400">{m.idNumber || <span className="text-gray-400">-</span>}</td>
                    <td className="px-3 py-2">
                      <Select value={state.roomSelection.assignments[m.id] || ""} onChange={(e) => assignMemberRoom(modeKey, m.id, e.target.value)} className="py-1.5">
                        <option value="">Select room</option>
                        {state.roomSelection.selectedRooms.map((rno) => (
                          <option key={rno} value={rno}>
                            Room {rno}
                          </option>
                        ))}
                      </Select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </SectionCard>
    );
  };

  // --- Pricing
  const renderPricing = (modeKey) => {
    const isSingle = modeKey === "single";
    const isGroup = modeKey === "group";
    const state = isSingle ? single : isGroup ? group : company;
    const setState = isSingle ? setSingle : isGroup ? setGroup : setCompany;

    return (
      <SectionCard icon="sell" title="Pricing, discount & reference" subtitle="Choose rate plan, apply discount code/manual discount, and add services.">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
            <p className="font-medium text-gray-900 dark:text-white mb-2.5">Rate plan</p>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
              {RATE_PLANS.map((plan) => {
                const selected = state.pricing.ratePlanId === plan.id;
                return (
                  <button
                    key={plan.id}
                    onClick={() => setState((p) => ({ ...p, pricing: { ...p.pricing, ratePlanId: plan.id } }))}
                    className={[
                      "rounded-lg border p-2.5 text-left transition text-sm",
                      selected ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20" : "border-gray-200 dark:border-gray-700 hover:border-blue-400 hover:bg-gray-50 dark:hover:bg-gray-700",
                    ].join(" ")}
                  >
                    <div className="flex items-center justify-between gap-1.5">
                      <p className="font-medium text-gray-900 dark:text-white">{plan.name}</p>
                      <Icon name="chevron_right" className="text-gray-400" />
                    </div>
                    <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">{plan.description}</p>
                    {plan.discount > 0 ? (
                      <div className="mt-1.5">
                        <Chip color="green">{plan.discount}% OFF</Chip>
                      </div>
                    ) : null}
                  </button>
                );
              })}
            </div>

            <Divider />
            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-3">
              <Field label="Reference source">
                <Select value={meta.referenceSource} onChange={(e) => setMeta((p) => ({ ...p, referenceSource: e.target.value }))} className="py-2">
                  <option value="walkin">Walk-in</option>
                  <option value="website">Website</option>
                  <option value="agent">Travel Agent</option>
                  <option value="phone">Phone</option>
                  <option value="corporate">Corporate</option>
                </Select>
              </Field>

              <Field label="Discount code">
                <div className="flex gap-1.5">
                  <Input value={meta.discountCode} onChange={(e) => setMeta((p) => ({ ...p, discountCode: e.target.value }))} placeholder="Enter code" className="py-2" />
                  <button
                    onClick={() => {
                      const code = meta.discountCode?.trim().toUpperCase();
                      if (!code) return showToast("Enter a discount code.", "warning");
                      if (!VALID_DISCOUNT_CODES[code]) return showToast("Invalid discount code.", "warning");
                      showToast(`Discount code applied: ${VALID_DISCOUNT_CODES[code]}%`, "success");
                    }}
                    className="px-3 py-2 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 text-sm"
                  >
                    Apply
                  </button>
                </div>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1.5">
                  Active discount: <span className="font-medium">{discountPercent}%</span>
                </p>
              </Field>

              <Field label="Manual discount (%)" hint="Adds to rate plan discount; capped at 80%.">
                <Input type="number" min={0} max={80} value={meta.manualDiscountPercent} onChange={(e) => setMeta((p) => ({ ...p, manualDiscountPercent: clamp(parseFloat(e.target.value || "0"), 0, 80) }))} className="py-2" />
              </Field>

              <Field label="Tax toggle">
                <label className="flex items-center justify-between rounded-lg border border-gray-200 dark:border-gray-700 p-3 cursor-pointer bg-gray-50 dark:bg-gray-800">
                  <span className="text-sm font-medium text-gray-800 dark:text-gray-200">Include 10% taxes</span>
                  <div className="relative">
                    <input type="checkbox" checked={meta.includeTax} onChange={(e) => setMeta((p) => ({ ...p, includeTax: e.target.checked }))} className="sr-only" />
                    <div className={`w-10 h-5 rounded-full transition ${meta.includeTax ? 'bg-blue-600' : 'bg-gray-300 dark:bg-gray-600'}`}>
                      <div className={`absolute top-0.5 left-0.5 w-4 h-4 rounded-full bg-white transition-transform ${meta.includeTax ? 'transform translate-x-5' : ''}`} />
                    </div>
                  </div>
                </label>
              </Field>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
            <p className="font-medium text-gray-900 dark:text-white mb-2.5">Additional services</p>
            <div className="space-y-2">
              {[
                { key: "parking", title: "Valet Parking", desc: "Daily valet parking service", price: "₹15/day/room" },
                { key: "breakfast", title: "Daily Breakfast", desc: "Buffet breakfast for guests", price: "₹18/day/guest" },
                { key: "lateCheckout", title: "Late Check-out", desc: "Late checkout until 2:00 PM", price: "₹30 one-time" },
              ].map((s) => (
                <label key={s.key} className="flex items-center justify-between gap-3 rounded-lg border border-gray-200 dark:border-gray-700 p-3 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700">
                  <div className="flex items-start gap-2.5">
                    <input
                      type="checkbox"
                      className="mt-0.5 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      checked={state.pricing.services[s.key]}
                      onChange={(e) => setState((p) => ({ ...p, pricing: { ...p.pricing, services: { ...p.pricing.services, [s.key]: e.target.checked } } }))}
                    />
                    <div>
                      <p className="font-medium text-gray-900 dark:text-white">{s.title}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400">{s.desc}</p>
                    </div>
                  </div>
                  <Chip color="gray">{s.price}</Chip>
                </label>
              ))}
            </div>

            <Divider />
            <div className="mt-3">
              <Field label="Internal notes (optional)">
                <Textarea rows={3} value={meta.notes} onChange={(e) => setMeta((p) => ({ ...p, notes: e.target.value }))} className="py-2" />
              </Field>
            </div>
          </div>
        </div>
      </SectionCard>
    );
  };

  // --- Billing
  const renderBilling = () => (
    <SectionCard icon="receipt_long" title="Billing review" subtitle="Review charges, discounts, taxes and totals before payment.">
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
          <p className="font-medium text-gray-900 dark:text-white mb-2.5">Selected rooms</p>
          <div className="space-y-1.5 mb-4">
            {selectedRoomObjects.length ? (
              selectedRoomObjects.map((r) => (
                <div key={r.number} className="flex items-center justify-between rounded border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 p-2.5">
                  <div>
                    <p className="font-medium text-gray-900 dark:text-white">Room {r.number}</p>
                    <p className="text-xs text-gray-600 dark:text-gray-400">
                      {ROOM_CATEGORIES.find((c) => c.id === r.category)?.label} • Floor {r.floor}
                    </p>
                  </div>
                  <p className="font-medium text-gray-900 dark:text-white">₹{r.rate}/night</p>
                </div>
              ))
            ) : (
              <div className="text-sm text-gray-600 dark:text-gray-400">No rooms selected yet.</div>
            )}
          </div>

          <Divider />
          <div className="mt-4 space-y-2 text-sm">
            <div className="flex items-center justify-between">
              <span className="text-gray-600 dark:text-gray-400">Nights</span>
              <span className="font-medium text-gray-900 dark:text-white">{totals.nights}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-gray-600 dark:text-gray-400">Room charges</span>
              <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.roomCharges)}</span>
            </div>
            {totals.discountAmount > 0 ? (
              <div className="flex items-center justify-between">
                <span className="text-gray-600 dark:text-gray-400">Discount ({discountPercent}%)</span>
                <span className="font-medium text-green-600 dark:text-green-400">- ₹{money(totals.discountAmount)}</span>
              </div>
            ) : null}
            <div className="flex items-center justify-between">
              <span className="text-gray-600 dark:text-gray-400">Services</span>
              <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.servicesTotal)}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-gray-600 dark:text-gray-400">Taxes (10%)</span>
              <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.taxes)}</span>
            </div>
            <Divider />
            <div className="flex items-center justify-between pt-1">
              <span className="font-semibold text-gray-900 dark:text-white">Total payable</span>
              <span className="text-xl font-bold text-blue-600 dark:text-blue-400">₹{money(totals.total)}</span>
            </div>
          </div>
        </div>

        <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
          <p className="font-medium text-gray-900 dark:text-white mb-2.5">Invoice preview</p>
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 p-3 text-sm text-gray-700 dark:text-gray-200">
            <div className="flex items-center justify-between">
              <span className="text-gray-600 dark:text-gray-400">Booking type</span>
              <span className="font-medium">{mode === "single" ? "Single" : mode === "group" ? "Group" : "Company"}</span>
            </div>
            <div className="flex items-center justify-between mt-1.5">
              <span className="text-gray-600 dark:text-gray-400">Reference</span>
              <span className="font-medium">{meta.referenceSource}</span>
            </div>
            <div className="flex items-center justify-between mt-1.5">
              <span className="text-gray-600 dark:text-gray-400">Rate plan</span>
              <span className="font-medium">{activeRatePlan.name}</span>
            </div>
            <div className="flex items-center justify-between mt-1.5">
              <span className="text-gray-600 dark:text-gray-400">Discount</span>
              <span className="font-medium">{discountPercent}%</span>
            </div>

            <Divider />
            <div className="mt-2.5">
              <p className="text-xs text-gray-500 dark:text-gray-400">Notes</p>
              <p className="mt-1 text-sm">{meta.notes?.trim() ? meta.notes : <span className="text-gray-400">No notes</span>}</p>
            </div>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-2">
            <button onClick={() => showToast("Invoice printed (mock).", "success")} className="py-2 rounded-lg border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 font-medium text-sm">
              <div className="flex items-center justify-center gap-1.5">
                <Icon name="print" />
                Print
              </div>
            </button>
            <button onClick={() => showToast("Draft saved (mock).", "success")} className="py-2 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 text-sm">
              <div className="flex items-center justify-center gap-1.5">
                <Icon name="save" />
                Save draft
              </div>
            </button>
          </div>
        </div>
      </div>
    </SectionCard>
  );

  // --- Payment
  const renderPayment = () => {
    const isSingle = mode === "single";
    const isGroup = mode === "group";
    const pay = isSingle ? single.payment : isGroup ? group.payment : company.payment;

    const setPay = (patch) => {
      if (isSingle) setSingle((p) => ({ ...p, payment: { ...p.payment, ...patch } }));
      else if (isGroup) setGroup((p) => ({ ...p, payment: { ...p.payment, ...patch } }));
      else setCompany((p) => ({ ...p, payment: { ...p.payment, ...patch } }));
    };

    const allowCorporateCredit = mode === "company" && pay.method === "corporate" && company.payment.billToCompany;

    return (
      <SectionCard icon="payments" title="Payment page" subtitle="Finalize payment based on billing total.">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
            <p className="font-medium text-gray-900 dark:text-white mb-3">Payment method</p>
            <div className="grid grid-cols-2 gap-1.5">
              {PAYMENT_METHODS.map((m) => (
                <button
                  key={m.id}
                  onClick={() => setPay({ method: m.id })}
                  className={[
                    "py-2 rounded border text-sm font-medium transition",
                    pay.method === m.id ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300" : "border-gray-300 dark:border-gray-700 hover:border-blue-400 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200",
                  ].join(" ")}
                >
                  {m.label}
                </button>
              ))}
            </div>

            {mode === "company" ? (
              <div className="mt-3 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 p-3">
                <label className="flex items-center justify-between cursor-pointer">
                  <span className="text-sm font-medium text-gray-800 dark:text-gray-200">Bill to company (pay later)</span>
                  <input
                    type="checkbox"
                    checked={!!company.payment.billToCompany}
                    onChange={(e) => setCompany((p) => ({ ...p, payment: { ...p.payment, billToCompany: e.target.checked } }))}
                    className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                  />
                </label>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-1.5">If enabled and method is Corporate Account, amount paid can be 0.</p>
              </div>
            ) : null}

            <Divider />
            <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3">
              <Field label="Payer name">
                <Input value={pay.payerName || ""} onChange={(e) => setPay({ payerName: e.target.value })} className="py-2" />
              </Field>
              <Field label="Transaction reference">
                <Input value={pay.txnRef || ""} onChange={(e) => setPay({ txnRef: e.target.value })} placeholder="UPI/Bank ref (optional)" className="py-2" />
              </Field>
              <Field label={`Amount paid (₹) ${allowCorporateCredit ? "(optional)" : ""}`} required={!allowCorporateCredit}>
                <Input type="number" value={pay.amountPaid} onChange={(e) => setPay({ amountPaid: e.target.value })} placeholder={allowCorporateCredit ? "0" : `${money(totals.total)}`} className="py-2" />
              </Field>

              <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 p-3">
                <p className="text-xs text-gray-500 dark:text-gray-400">Total payable</p>
                <p className="text-xl font-bold text-blue-600 dark:text-blue-400 mt-0.5">₹{money(totals.total)}</p>
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                  Taxes: ₹{money(totals.taxes)} • Discount: ₹{money(totals.discountAmount)}
                </p>
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
            <p className="font-medium text-gray-900 dark:text-white mb-2.5">Confirm</p>
            <div className="space-y-2 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-gray-600 dark:text-gray-400">Rooms selected</span>
                <span className="font-medium text-gray-900 dark:text-white">{selectedRoomObjects.length || 0}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600 dark:text-gray-400">Amount paid</span>
                <span className="font-medium text-gray-900 dark:text-white">₹{money(Number(pay.amountPaid || 0))}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-gray-600 dark:text-gray-400">Balance</span>
                <span className="font-medium text-gray-900 dark:text-white">₹{money(Math.max(0, totals.total - Number(pay.amountPaid || 0)))}</span>
              </div>

              <Divider />

              <button
                onClick={completeBooking}
                className="w-full py-2.5 rounded-lg bg-green-600 text-white font-medium hover:bg-green-700 transition flex items-center justify-center gap-1.5"
              >
                <Icon name="check_circle" />
                Confirm & Complete
              </button>
              <p className="text-xs text-gray-500 dark:text-gray-400 text-center">This will finalize the booking (mock submit) and open quick actions.</p>
            </div>
          </div>
        </div>
      </SectionCard>
    );
  };

  // --- Group/Company Search, Leader/Company Info, Members
  const renderGroupSearch = () => (
    <SectionCard
      icon="person_search"
      title="Search group leader details"
      subtitle="Search existing leader profile (optional)."
      right={
        <button
          onClick={() => {
            setGroupSearch((p) => ({ ...p, open: false }));
            showToast("Proceeding without search.", "info");
            goNext();
          }}
          className="text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white"
        >
          Skip
        </button>
      }
    >
      <div className="relative">
        <div className="flex items-center gap-3 bg-gray-50 dark:bg-gray-800 rounded-lg px-3.5 py-2.5 border border-gray-200 dark:border-gray-700 focus-within:ring-2 focus-within:ring-purple-100 dark:focus-within:ring-purple-900/20">
          <Icon name="search" className="text-gray-400" />
          <Input placeholder="Search leader..." value={groupSearch.query} onChange={(e) => doGuestSearch(e.target.value, setGroupSearch)} onFocus={() => setGroupSearch((p) => ({ ...p, open: p.results.length > 0 }))} className="border-0 bg-transparent px-0 py-0 focus:ring-0 focus:border-0" />
        </div>

        {groupSearch.open ? (
          <div className="absolute z-40 w-full mt-1.5 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-lg max-h-72 overflow-y-auto">
            {groupSearch.results.length ? (
              groupSearch.results.map((g) => (
                <button key={g.id} className="w-full text-left p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-100 dark:border-gray-700 last:border-b-0" onClick={() => selectGuestIntoGroupLeader(g)}>
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="font-medium text-gray-900 dark:text-white truncate">{g.name}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400 truncate">
                        {g.email} • {g.phone}
                      </p>
                    </div>
                    <Icon name="arrow_forward" className="text-gray-400" />
                  </div>
                </button>
              ))
            ) : (
              <div className="p-3 text-sm text-gray-600 dark:text-gray-400">No matches.</div>
            )}
            <div className="p-3">
              <button
                onClick={() => {
                  setGroupSearch((p) => ({ ...p, open: false }));
                  goNext();
                }}
                className="w-full px-4 py-2.5 rounded-lg bg-purple-600 text-white font-medium hover:bg-purple-700 transition"
              >
                Continue with new leader
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </SectionCard>
  );

  const renderCompanySearch = () => (
    <SectionCard
      icon="domainsearch"
      title="Search company detail"
      subtitle="Search corporate account (optional)."
      right={
        <button
          onClick={() => {
            setCompanySearch((p) => ({ ...p, open: false }));
            showToast("Proceeding without search.", "info");
            goNext();
          }}
          className="text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-gray-800 dark:hover:text-white"
        >
          Skip
        </button>
      }
    >
      <div className="relative">
        <div className="flex items-center gap-3 bg-gray-50 dark:bg-gray-800 rounded-lg px-3.5 py-2.5 border border-gray-200 dark:border-gray-700 focus-within:ring-2 focus-within:ring-amber-100 dark:focus-within:ring-amber-900/20">
          <Icon name="search" className="text-gray-400" />
          <Input placeholder="Search company..." value={companySearch.query} onChange={(e) => doCompanySearch(e.target.value)} onFocus={() => setCompanySearch((p) => ({ ...p, open: p.results.length > 0 }))} className="border-0 bg-transparent px-0 py-0 focus:ring-0 focus:border-0" />
        </div>

        {companySearch.open ? (
          <div className="absolute z-40 w-full mt-1.5 bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 shadow-lg max-h-72 overflow-y-auto">
            {companySearch.results.length ? (
              companySearch.results.map((c) => (
                <button key={c.id} className="w-full text-left p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-100 dark:border-gray-700 last:border-b-0" onClick={() => selectCompany(c)}>
                  <div className="flex items-center justify-between gap-3">
                    <div className="min-w-0">
                      <p className="font-medium text-gray-900 dark:text-white truncate">{c.name}</p>
                      <p className="text-sm text-gray-600 dark:text-gray-400 truncate">
                        {c.taxId} • {c.contactEmail}
                      </p>
                    </div>
                    <Icon name="arrow_forward" className="text-gray-400" />
                  </div>
                </button>
              ))
            ) : (
              <div className="p-3 text-sm text-gray-600 dark:text-gray-400">No matches.</div>
            )}
            <div className="p-3">
              <button
                onClick={() => {
                  setCompanySearch((p) => ({ ...p, open: false }));
                  goNext();
                }}
                className="w-full px-4 py-2.5 rounded-lg bg-amber-600 text-white font-medium hover:bg-amber-700 transition"
              >
                Continue with new company
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </SectionCard>
  );

  const renderCompanyInfo = () => (
    <SectionCard icon="corporate_fare" title="Company info" subtitle="Company billing details.">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Field label="Company name" required>
          <Input value={company.company.name} onChange={(e) => setCompany((p) => ({ ...p, company: { ...p.company, name: e.target.value } }))} />
        </Field>
        <Field label="Company Tax ID / GST" required>
          <Input value={company.company.taxId} onChange={(e) => setCompany((p) => ({ ...p, company: { ...p.company, taxId: e.target.value } }))} />
        </Field>
        <div className="md:col-span-2">
          <Field label="Company address">
            <Textarea rows={2} value={company.company.address} onChange={(e) => setCompany((p) => ({ ...p, company: { ...p.company, address: e.target.value } }))} />
          </Field>
        </div>
        <Field label="Company contact email">
          <Input value={company.company.contactEmail} onChange={(e) => setCompany((p) => ({ ...p, company: { ...p.company, contactEmail: e.target.value } }))} />
        </Field>
        <Field label="Company contact phone">
          <Input value={company.company.contactPhone} onChange={(e) => setCompany((p) => ({ ...p, company: { ...p.company, contactPhone: e.target.value } }))} />
        </Field>
      </div>
    </SectionCard>
  );

  const renderLeaderInfo = (modeKey) => {
    const isGroup = modeKey === "group";
    const leader = isGroup ? group.leader.info : company.leader.info;
    const setLeader = (patch) => {
      if (isGroup) setGroup((p) => ({ ...p, leader: { ...p.leader, info: { ...p.leader.info, ...patch } } }));
      else setCompany((p) => ({ ...p, leader: { ...p.leader, info: { ...p.leader.info, ...patch } } }));
    };

    return (
      <SectionCard icon="person" title={isGroup ? "Group leader info" : "Company contact info"} subtitle="Primary person for documentation and coordination.">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Field label="First name" required>
            <Input value={leader.firstName} onChange={(e) => setLeader({ firstName: e.target.value })} />
          </Field>
          <Field label="Last name" required>
            <Input value={leader.lastName} onChange={(e) => setLeader({ lastName: e.target.value })} />
          </Field>
          <Field label="Email">
            <Input type="email" value={leader.email || ""} onChange={(e) => setLeader({ email: e.target.value })} />
          </Field>
          <Field label="Phone" required>
            <div className="flex gap-2">
              <Select value={leader.countryCode || "+91"} onChange={(e) => setLeader({ countryCode: e.target.value })} className="w-28">
                <option value="+91">+91 IN</option>
                <option value="+1">+1 US</option>
                <option value="+44">+44 UK</option>
                <option value="+61">+61 AU</option>
              </Select>
              <Input value={leader.phone || ""} onChange={(e) => setLeader({ phone: e.target.value })} />
            </div>
          </Field>

          {isGroup ? (
            <>
              <Field label="Nationality">
                <Select value={leader.nationality || "IN"} onChange={(e) => setLeader({ nationality: e.target.value })}>
                  <option value="IN">India</option>
                  <option value="US">United States</option>
                  <option value="UK">United Kingdom</option>
                  <option value="CA">Canada</option>
                  <option value="AU">Australia</option>
                </Select>
              </Field>
              <Field label="Date of birth">
                <Input type="date" value={leader.dob || ""} onChange={(e) => setLeader({ dob: e.target.value })} />
              </Field>
            </>
          ) : null}

          <Field label="Address">
            <Textarea rows={2} value={leader.address || ""} onChange={(e) => setLeader({ address: e.target.value })} />
          </Field>

          <Field label="ID type" required={isGroup}>
            <Select value={leader.idType || "aadhaar"} onChange={(e) => setLeader({ idType: e.target.value })}>
              {ID_TYPES.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.label}
                </option>
              ))}
            </Select>
          </Field>

          <Field label="ID number" required={isGroup}>
            <Input value={leader.idNumber || ""} onChange={(e) => setLeader({ idNumber: e.target.value })} />
          </Field>
        </div>
      </SectionCard>
    );
  };

  const renderLeaderDocs = (modeKey) => {
    const docs = modeKey === "group" ? group.leader.documents : company.leader.documents;
    const isGroup = modeKey === "group";

    return (
      <SectionCard icon="description" title={isGroup ? "Leader documents & photo" : "Contact documents & photo"} subtitle="Mandatory for booking completion.">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <DocBox title="ID proof" required uploadedName={docs.idProofName} onClear={() => onUploadLeader(modeKey, "idProof", null)}>
            <label className="w-full cursor-pointer">
              <div className="rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-4 text-center transition">
                <Icon name="cloud_upload" className="text-gray-500" />
                <p className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload ID document</p>
                <p className="text-xs text-gray-500 dark:text-gray-400">PNG / JPG / PDF</p>
              </div>
              <input type="file" className="hidden" accept=".png,.jpg,.jpeg,.pdf" onChange={(e) => e.target.files?.[0] && onUploadLeader(modeKey, "idProof", e.target.files[0])} />
            </label>
          </DocBox>

          <DocBox title="Photo" required uploadedName={docs.guestPhotoName} onClear={() => onUploadLeader(modeKey, "guestPhoto", null)}>
            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => openCamera({ kind: isGroup ? "groupLeaderPhoto" : "companyLeaderPhoto" })}
                className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center"
              >
                <Icon name="photo_camera" className="text-gray-700 dark:text-gray-200" />
                <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Use camera</span>
              </button>

              <label className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center cursor-pointer">
                <Icon name="upload" className="text-gray-700 dark:text-gray-200" />
                <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload photo</span>
                <input type="file" className="hidden" accept="image/png,image/jpeg,image/jpg" onChange={(e) => e.target.files?.[0] && onUploadLeader(modeKey, "guestPhoto", e.target.files[0])} />
              </label>
            </div>
          </DocBox>
        </div>
      </SectionCard>
    );
  };

  const renderMembers = (modeKey) => {
    const isGroup = modeKey === "group";
    const members = isGroup ? group.members : company.members;
    const selectedRooms = isGroup ? group.roomSelection.selectedRooms : company.roomSelection.selectedRooms;

    return (
      <SectionCard
        icon="groups"
        title="Members details & documents"
        subtitle="Add members, upload/capture documents, and later assign each member to a room."
        right={
          <button onClick={() => addMember(modeKey)} className={["px-3 py-1.5 rounded-lg font-medium text-white hover:opacity-95 transition text-sm", isGroup ? "bg-purple-600" : "bg-amber-600"].join(" ")}>
            <div className="flex items-center gap-1.5">
              <Icon name="person_add" />
              Add member
            </div>
          </button>
        }
      >
        {members.length === 0 ? (
          <div className="rounded-lg border border-dashed border-gray-300 dark:border-gray-700 p-6 text-center">
            <Icon name="group_add" className="text-gray-500 text-[32px]" />
            <p className="mt-2 font-medium text-gray-900 dark:text-white">No members added</p>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-0.5">Add group/company members to proceed.</p>
          </div>
        ) : null}

        <div className="mt-4 space-y-3">
          {members.map((m) => (
            <div key={m.id} className="rounded-lg border border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-4">
              <div className="flex items-start justify-between gap-3 mb-3">
                <div className="min-w-0">
                  <p className="font-medium text-gray-900 dark:text-white truncate">{m.firstName || m.lastName ? `${m.firstName} ${m.lastName}` : "Member"}</p>
                  <p className="text-xs text-gray-600 dark:text-gray-400 mt-0.5">Room: {m.roomNo ? `Room ${m.roomNo}` : "Not assigned"} • ID: {m.idNumber || "-"}</p>
                </div>
                <button onClick={() => removeMember(modeKey, m.id)} className="p-1.5 rounded hover:bg-red-50 dark:hover:bg-red-900/20 text-red-500">
                  <Icon name="delete" />
                </button>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <Field label="First name" required>
                  <Input value={m.firstName} onChange={(e) => updateMember(modeKey, m.id, { firstName: e.target.value })} />
                </Field>
                <Field label="Last name" required>
                  <Input value={m.lastName} onChange={(e) => updateMember(modeKey, m.id, { lastName: e.target.value })} />
                </Field>
                <Field label="ID type" required>
                  <Select value={m.idType} onChange={(e) => updateMember(modeKey, m.id, { idType: e.target.value })}>
                    {ID_TYPES.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.label}
                      </option>
                    ))}
                  </Select>
                </Field>
                <Field label="ID number" required>
                  <Input value={m.idNumber} onChange={(e) => updateMember(modeKey, m.id, { idNumber: e.target.value })} />
                </Field>

                <Field label="Room assignment (optional here)" hint="Final assignment is required in Rooms & Assign step.">
                  <Select value={m.roomNo || ""} onChange={(e) => assignMemberRoom(modeKey, m.id, e.target.value)} disabled={!selectedRooms.length} className="py-2">
                    <option value="">{selectedRooms.length ? "Select from selected rooms" : "Select rooms first"}</option>
                    {selectedRooms.map((rno) => (
                      <option key={rno} value={rno}>
                        Room {rno}
                      </option>
                    ))}
                  </Select>
                </Field>
              </div>

              <div className="mt-4 grid grid-cols-1 lg:grid-cols-2 gap-3">
                <DocBox title="Member ID proof" required uploadedName={m.documents.idProofName} onClear={() => onUploadMember(modeKey, m.id, "idProof", null)}>
                  <label className="w-full cursor-pointer">
                    <div className="rounded-lg border-2 border-dashed border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-4 text-center transition">
                      <Icon name="cloud_upload" className="text-gray-500" />
                      <p className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload ID document</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">PNG / JPG / PDF</p>
                    </div>
                    <input type="file" className="hidden" accept=".png,.jpg,.jpeg,.pdf" onChange={(e) => e.target.files?.[0] && onUploadMember(modeKey, m.id, "idProof", e.target.files[0])} />
                  </label>
                </DocBox>

                <DocBox title="Member photo" required uploadedName={m.documents.guestPhotoName} onClear={() => onUploadMember(modeKey, m.id, "guestPhoto", null)}>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => openCamera({ kind: modeKey === "group" ? "groupMemberPhoto" : "companyMemberPhoto", memberId: m.id })}
                      className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center"
                    >
                      <Icon name="photo_camera" className="text-gray-700 dark:text-gray-200" />
                      <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Use camera</span>
                    </button>

                    <label className="rounded-lg border border-gray-300 dark:border-gray-700 hover:border-blue-500 hover:bg-blue-50/60 dark:hover:bg-blue-900/10 p-3 transition flex flex-col items-center cursor-pointer">
                      <Icon name="upload" className="text-gray-700 dark:text-gray-200" />
                      <span className="text-sm font-medium text-gray-900 dark:text-white mt-1.5">Upload photo</span>
                      <input type="file" className="hidden" accept="image/png,image/jpeg,image/jpg" onChange={(e) => e.target.files?.[0] && onUploadMember(modeKey, m.id, "guestPhoto", e.target.files[0])} />
                    </label>
                  </div>
                </DocBox>
              </div>
            </div>
          ))}
        </div>
      </SectionCard>
    );
  };

  // ========= Step Switch =========
  const renderStep = () => {
    const key = steps[stepIndex]?.key;

    if (mode === "single") {
      if (key === "search") return renderSingleSearch();
      if (key === "guest") return renderSingleGuestInfo();
      if (key === "docs") return renderSingleDocs();
      if (key === "stay") return renderStayDetails("single");
      if (key === "room") return renderSingleRoomSelection();
      if (key === "pricing") return renderPricing("single");
      if (key === "billing") return renderBilling();
      if (key === "payment") return renderPayment();
    }

    if (mode === "group") {
      if (key === "search") return renderGroupSearch();
      if (key === "leader") return renderLeaderInfo("group");
      if (key === "docs") return renderLeaderDocs("group");
      if (key === "members") return renderMembers("group");
      if (key === "stay") return renderStayDetails("group");
      if (key === "room") return renderMultiRoomSelection("group");
      if (key === "pricing") return renderPricing("group");
      if (key === "billing") return renderBilling();
      if (key === "payment") return renderPayment();
    }

    // company
    if (key === "search") return renderCompanySearch();
    if (key === "company") return renderCompanyInfo();
    if (key === "leader") return renderLeaderInfo("company");
    if (key === "docs") return renderLeaderDocs("company");
    if (key === "members") return renderMembers("company");
    if (key === "stay") return renderStayDetails("company");
    if (key === "room") return renderMultiRoomSelection("company");
    if (key === "pricing") return renderPricing("company");
    if (key === "billing") return renderBilling();
    if (key === "payment") return renderPayment();

    return null;
  };

  // ========= Summary Card =========
  const docsStatus = useMemo(() => {
    if (mode === "single") return { idProof: !!single.documents.idProof, photo: !!single.documents.guestPhoto };
    if (mode === "group") return { idProof: !!group.leader.documents.idProof, photo: !!group.leader.documents.guestPhoto };
    return { idProof: !!company.leader.documents.idProof, photo: !!company.leader.documents.guestPhoto };
  }, [mode, single.documents.idProof, single.documents.guestPhoto, group.leader.documents.idProof, group.leader.documents.guestPhoto, company.leader.documents.idProof, company.leader.documents.guestPhoto]);

  const selectedRoomsText = useMemo(() => {
    if (!selectedRoomObjects.length) return "Not selected";
    return selectedRoomObjects.map((r) => r.number).join(", ");
  }, [selectedRoomObjects]);

  const SummaryCard = () => (
    <div className="sticky top-6 space-y-3">
      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
        <div className="p-4 border-b border-gray-100 dark:border-gray-700">
          <div className="flex items-center justify-between gap-2">
            <h3 className="font-semibold text-gray-900 dark:text-white">Summary</h3>
            <Chip color="gray">{mode === "single" ? "Single" : mode === "group" ? "Group" : "Company"}</Chip>
          </div>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Billing + payment snapshot</p>
        </div>

        <div className="p-4 space-y-2 text-sm">
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Rooms</span>
            <span className="font-medium text-gray-900 dark:text-white">{selectedRoomsText}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Nights</span>
            <span className="font-medium text-gray-900 dark:text-white">{totals.nights}</span>
          </div>
          <Divider />
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Room charges</span>
            <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.roomCharges)}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Discount</span>
            <span className="font-medium text-green-600 dark:text-green-400">₹{money(totals.discountAmount)}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Services</span>
            <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.servicesTotal)}</span>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Taxes</span>
            <span className="font-medium text-gray-900 dark:text-white">₹{money(totals.taxes)}</span>
          </div>
          <Divider />
          <div className="flex items-center justify-between pt-1">
            <span className="font-semibold text-gray-900 dark:text-white">Total</span>
            <span className="text-lg font-bold text-blue-600 dark:text-blue-400">₹{money(totals.total)}</span>
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4">
        <h4 className="font-medium text-gray-900 dark:text-white mb-2">Document status</h4>
        <div className="space-y-1.5 text-sm">
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">ID proof</span>
            {docsStatus.idProof ? <Chip color="green">Uploaded</Chip> : <Chip color="yellow">Pending</Chip>}
          </div>
          <div className="flex items-center justify-between">
            <span className="text-gray-600 dark:text-gray-400">Photo</span>
            {docsStatus.photo ? <Chip color="green">Uploaded</Chip> : <Chip color="yellow">Pending</Chip>}
          </div>
        </div>
        <button onClick={() => setShowRoomModal(true)} className="w-full mt-3 px-3 py-1.5 rounded-lg border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 font-medium text-sm">
          View room status
        </button>
      </div>

      <div className="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 p-4">
        <h4 className="font-medium text-gray-900 dark:text-white mb-2">Quick actions</h4>
        <div className="grid grid-cols-2 gap-2">
          <button onClick={() => showToast("Draft saved (mock).", "success")} className="p-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm font-medium">
            <div className="flex flex-col items-center gap-1">
              <Icon name="save" className="text-blue-600 dark:text-blue-400" />
              Save Draft
            </div>
          </button>
          <button onClick={resetAll} className="p-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 text-sm font-medium">
            <div className="flex flex-col items-center gap-1">
              <Icon name="refresh" className="text-blue-600 dark:text-blue-400" />
              Reset
            </div>
          </button>
        </div>
      </div>
    </div>
  );

  // ========= Bottom Bar =========
  const isLastStep = stepIndex === steps.length - 1;
  const nextLabel = isLastStep ? "Complete" : "Next";

  // ========= Main Layout =========
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-700 dark:text-gray-200">
      {/* Header */}
      <div className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
            <div>
              <h1 className="text-xl font-bold text-gray-900 dark:text-white">Walk-In Booking / Check-In</h1>
              <p className="text-gray-600 dark:text-gray-400 text-sm mt-0.5">Single • Group • Company booking with documents, rooms, billing & payment.</p>
            </div>
            <div className="w-full lg:w-96">{renderModeTabs()}</div>
          </div>
        </div>
      </div>

      {/* Stepper */}
      {renderStepper()}

      {/* Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2 space-y-4">{renderStep()}</div>
          <div className="space-y-4">
            <SummaryCard />
          </div>
        </div>
      </div>

      {/* Bottom Action Bar */}
      <div className="sticky bottom-0 bg-white/95 dark:bg-gray-900/95 border-t border-gray-200 dark:border-gray-800 py-3 px-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between gap-3">
          <div className="flex items-center gap-1.5">
            <button
              onClick={goPrev}
              disabled={stepIndex === 0}
              className={[
                "flex items-center gap-1.5 px-3 py-1.5 rounded border text-sm font-medium transition",
                stepIndex === 0 ? "opacity-50 cursor-not-allowed border-gray-200 dark:border-gray-800" : "border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800",
              ].join(" ")}
            >
              <Icon name="arrow_back" />
              Previous
            </button>

            <button onClick={resetAll} className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 text-sm font-medium hover:bg-gray-50 dark:hover:bg-gray-800">
              <Icon name="refresh" />
              Reset
            </button>
          </div>

          <button
            onClick={() => (isLastStep ? completeBooking() : goNext())}
            className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-blue-600 text-white font-medium hover:bg-blue-700 transition"
          >
            {nextLabel}
            <Icon name={isLastStep ? "check_circle" : "arrow_forward"} />
          </button>
        </div>
      </div>

      {/* Toast */}
      {toast.show ? (
        <div
          className={[
            "fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg border fade-in max-w-sm",
            toast.type === "success"
              ? "bg-green-50 dark:bg-green-900/30 border-green-200 dark:border-green-800 text-green-800 dark:text-green-300"
              : toast.type === "warning"
              ? "bg-yellow-50 dark:bg-yellow-900/30 border-yellow-200 dark:border-yellow-800 text-yellow-800 dark:text-yellow-300"
              : "bg-blue-50 dark:bg-blue-900/30 border-blue-200 dark:border-blue-800 text-blue-800 dark:text-blue-300",
          ].join(" ")}
        >
          <div className="flex items-start justify-between gap-2.5">
            <div className="flex items-start gap-2">
              <Icon name={toast.type === "success" ? "check_circle" : toast.type === "warning" ? "warning" : "info"} className="mt-0.5" />
              <p className="font-medium text-sm">{toast.message}</p>
            </div>
            <button onClick={() => setToast((t) => ({ ...t, show: false }))} className="p-0.5 rounded hover:bg-black/5 dark:hover:bg-white/10">
              <Icon name="close" />
            </button>
          </div>
        </div>
      ) : null}

      {/* Room Status Modal */}
      {showRoomModal ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
          <div className="bg-white dark:bg-gray-900 rounded-lg w-full max-w-4xl max-h-[90vh] overflow-y-auto border border-gray-200 dark:border-gray-800 shadow-lg">
            <div className="p-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Room Status</h3>
                <p className="text-sm text-gray-600 dark:text-gray-400 mt-0.5">
                  Available: <span className="font-semibold text-green-600">{status.available}</span> • Cleaning:{" "}
                  <span className="font-semibold text-yellow-600">{status.cleaning}</span> • Maintenance:{" "}
                  <span className="font-semibold text-red-600">{status.maintenance}</span>
                </p>
              </div>
              <button onClick={() => setShowRoomModal(false)} className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800">
                <Icon name="close" />
              </button>
            </div>

            <div className="p-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {AVAILABLE_ROOMS.map((room) => (
                <div
                  key={room.number}
                  className={[
                    "rounded-lg border p-3 text-sm",
                    room.status === "available"
                      ? "bg-green-50 dark:bg-green-900/20 border-green-200 dark:border-green-800"
                      : room.status === "cleaning"
                      ? "bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800"
                      : "bg-red-50 dark:bg-red-900/20 border-red-200 dark:border-red-800",
                  ].join(" ")}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-gray-900 dark:text-white">Room {room.number}</span>
                    {room.status === "available" ? <Chip color="green">Available</Chip> : room.status === "cleaning" ? <Chip color="yellow">Cleaning</Chip> : <Chip color="red">Maintenance</Chip>}
                  </div>
                  <p className="text-gray-600 dark:text-gray-400 mt-0.5">
                    {ROOM_CATEGORIES.find((c) => c.id === room.category)?.label} • ₹{room.rate}/night
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Floor {room.floor} • {room.amenities.slice(0, 2).join(", ")}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      ) : null}

      {/* Camera Modal */}
      {camera.open ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
          <div className="bg-white dark:bg-gray-900 rounded-lg w-full max-w-2xl border border-gray-200 dark:border-gray-800 shadow-lg overflow-hidden">
            <div className="p-4 border-b border-gray-200 dark:border-gray-800 flex items-center justify-between">
              <h3 className="font-semibold text-gray-900 dark:text-white">Capture photo</h3>
              <button
                onClick={async () => {
                  await stopCamera();
                  setCamera({ open: false, target: null });
                }}
                className="p-1.5 rounded hover:bg-gray-100 dark:hover:bg-gray-800"
              >
                <Icon name="close" />
              </button>
            </div>

            <div className="p-4">
              <div className="rounded-lg overflow-hidden border border-gray-200 dark:border-gray-800 bg-black">
                <video ref={videoRef} autoPlay playsInline className="w-full h-64 object-cover" />
              </div>

              <div className="mt-3 flex items-center justify-end gap-2">
                <button
                  onClick={async () => {
                    await stopCamera();
                    setCamera({ open: false, target: null });
                  }}
                  className="px-3 py-1.5 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm"
                >
                  Cancel
                </button>
                <button onClick={capturePhoto} className="px-4 py-1.5 rounded bg-blue-600 text-white font-medium hover:bg-blue-700 flex items-center gap-1.5 text-sm">
                  <Icon name="photo_camera" />
                  Capture
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : null}

      {/* Success Modal */}
      {showSuccessModal ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50">
          <div className="bg-white dark:bg-gray-900 rounded-lg w-full max-w-md border border-gray-200 dark:border-gray-800 shadow-lg p-6 text-center">
            <div className="w-14 h-14 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center mx-auto mb-4">
              <Icon name="check_circle" className="text-green-600 dark:text-green-400 text-3xl" />
            </div>
            <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-1.5">Booking Complete!</h3>
            <p className="text-gray-600 dark:text-gray-400 text-sm mb-4">The booking has been finalized (mock). Use quick actions below.</p>

            <div className="p-3 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 text-left mb-4">
              <p className="text-xs text-gray-600 dark:text-gray-400">Rooms</p>
              <p className="font-bold text-blue-600 dark:text-blue-400">{selectedRoomsText}</p>
              <p className="text-xs text-gray-600 dark:text-gray-400 mt-1.5">Total</p>
              <p className="font-bold text-gray-900 dark:text-white">₹{money(totals.total)}</p>
            </div>

            <div className="grid grid-cols-2 gap-2 mb-4">
              <button onClick={() => showToast("Registration card generated (mock).", "success")} className="py-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm">
                <div className="flex items-center justify-center gap-1.5">
                  <Icon name="badge" />
                  Reg Card
                </div>
              </button>
              <button onClick={() => showToast("Room key assigned (mock).", "success")} className="py-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm">
                <div className="flex items-center justify-center gap-1.5">
                  <Icon name="key" />
                  Room Key
                </div>
              </button>
              <button onClick={() => showToast("Welcome SMS sent (mock).", "success")} className="py-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm">
                <div className="flex items-center justify-center gap-1.5">
                  <Icon name="sms" />
                  SMS
                </div>
              </button>
              <button onClick={() => showToast("Folio printed (mock).", "success")} className="py-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm">
                <div className="flex items-center justify-center gap-1.5">
                  <Icon name="receipt" />
                  Folio
                </div>
              </button>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <button
                onClick={() => {
                  setShowSuccessModal(false);
                  resetAll();
                }}
                className="py-2 rounded border border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-800 font-medium text-sm"
              >
                New Booking
              </button>
              <button onClick={() => setShowSuccessModal(false)} className="py-2 rounded bg-blue-600 text-white font-medium hover:bg-blue-700 text-sm">
                Close
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {/* Styles */}
      <style jsx>{`
        .fade-in {
          animation: fadeIn 0.2s ease-out;
        }
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(6px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
          
      `}</style>
    </div>
  );

  
}