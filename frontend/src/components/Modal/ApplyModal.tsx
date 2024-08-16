import React, { useState } from 'react';
import {
  Button,
  Field,
  Fieldset,
  Input,
  Label,
  Legend,
  Textarea,
} from '@headlessui/react';
import { BoardType } from '../../types/BoardType';
import axiosInstance from '../../api/axiosInstance';

interface ModalType {
  boardType: BoardType;
  boardId: number;
}
const ApplyModal = ({ boardType, boardId }: ModalType) => {
  // State for form inputs and validation errors
  const [serverError, setServerError] = useState(''); // State for server error
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
  });
  const [errors, setErrors] = useState({
    name: '',
    phone: '',
    address: '',
  });

  // Handle input change and update state
  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
    setErrors({
      ...errors,
      [name]: '', // Reset error message when user starts typing
    });
  };

  // Validation function
  const validateForm = () => {
    const newErrors = { name: '', phone: '', address: '' };
    let isValid = true;

    if (formData.name.trim() === '') {
      newErrors.name = '이름을 입력해주세요.';
      isValid = false;
    }
    if (!/^\d{3}-\d{3,4}-\d{4}$/.test(formData.phone)) {
      newErrors.phone = '유효한 전화번호를 입력해주세요. 예: 010-1234-5678';
      isValid = false;
    }
    if (formData.address.trim() === '') {
      newErrors.address = '주소를 입력해주세요.';
      isValid = false;
    }

    setErrors(newErrors);
    return isValid;
  };

  // Handle form submission
  const handleSubmit = (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    if (validateForm()) {
      axiosInstance
        .post(`/${boardType}/${boardId}/participants`, formData)
        .then(() => {
          window.location.reload();
        })
        .catch((err) => {
          setServerError('에러가 발생했습니다. 다시 시도해 주세요.');
          console.error('Error:', err); // Log error for debugging
        });
    }
  };

  return (
    <dialog id="apply_modal" className="modal">
      <form className="modal-box px-8 max-w-sm">
        <Fieldset className="flex flex-col gap-4 text-disabled">
          <Legend className="text-xl font-semibold mb-2 text-neutral-500">
            {boardType === 'teatimes' ? '티타임' : '나눔'} 신청
          </Legend>
          <Field>
            <Label className="block">이름</Label>
            <Input
              className="mt-1 block border-2 px-2 py-1.5 w-full rounded focus:outline-none transition focus:border-blue-200"
              name="name"
              maxLength={10}
              value={formData.name}
              onChange={handleInputChange}
            />
            {errors.name && (
              <p className="text-red-500 text-sm mt-1">{errors.name}</p>
            )}
          </Field>
          <Field>
            <Label className="block">전화번호</Label>
            <Input
              type="tel"
              className="mt-1 block border-2 px-2 py-1.5 w-full rounded focus:outline-none transition focus:border-blue-200"
              name="phone"
              placeholder="010-1234-5678"
              pattern="\d{3}-\d{3}-\d{4}"
              value={formData.phone}
              onChange={handleInputChange}
            />
            {errors.phone && (
              <p className="text-red-500 text-sm mt-1">{errors.phone}</p>
            )}
          </Field>
          <Field>
            <Label className="block">주소</Label>
            <Textarea
              className="mt-1 block border-2 px-2 py-1.5 w-full rounded focus:outline-none transition focus:border-blue-200"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
            />
            {errors.address && (
              <p className="text-red-500 text-sm mt-1">{errors.address}</p>
            )}
          </Field>
          <Button
            type="submit"
            onClick={handleSubmit}
            className="btn text-center text-lg font-bold text-blue-500 bg-blue-100 rounded-lg hover:bg-blue-200 hover:border-none"
          >
            신청하기
          </Button>
          {serverError && (
            <p className="text-red-500 text-sm mt-2 text-center">
              {serverError}
            </p>
          )}
        </Fieldset>
      </form>
      <form method="dialog" className="modal-backdrop">
        <button>close</button>
      </form>
    </dialog>
  );
};

export default ApplyModal;
